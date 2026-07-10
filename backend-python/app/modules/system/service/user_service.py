from datetime import datetime

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession
from redis.asyncio import Redis

from app.common.errors import BusinessError, bad_request, forbidden, not_found
from app.common.id import new_id
from app.common.pagination import PageQuery, PageResult
from app.common.password import hash_password, new_salt
from app.modules.system.dto.user_request import PasswordResetRequest, RoleAssignRequest, StatusUpdateRequest, UserSaveRequest
from app.modules.system.entity import SysDept, SysRole, SysUser, SysUserRole
from app.modules.system.service.permission_service import PermissionService
from app.modules.system.service.login_security_service import LoginSecurityService
from app.modules.system.vo.user_vo import DeptSummaryVo, RoleSummaryVo, UserDetailVo, UserListVo


class UserService:
    def __init__(self, db: AsyncSession, redis: Redis) -> None:
        self.db = db
        self.redis = redis
        self.permissions = PermissionService(db)

    async def list_users(
        self, page: PageQuery, username: str | None, real_name: str | None, phone: str | None, status: int | None,
        dept_id: int | None,
        role_id: int | None,
        created_from: datetime | None,
        created_to: datetime | None,
    ) -> PageResult[UserListVo]:
        filters = [SysUser.deleted == 0]
        if username:
            filters.append(SysUser.username.like(f"%{username.strip()}%"))
        if real_name:
            filters.append(SysUser.real_name.like(f"%{real_name.strip()}%"))
        if phone:
            filters.append(SysUser.phone.like(f"%{phone.strip()}%"))
        if status is not None:
            filters.append(SysUser.status == status)
        if dept_id is not None:
            filters.append(SysUser.dept_id == dept_id)
        if role_id is not None:
            filters.append(SysUser.id.in_(select(SysUserRole.user_id).where(SysUserRole.role_id == role_id)))
        if created_from is not None:
            filters.append(SysUser.created_at >= created_from)
        if created_to is not None:
            filters.append(SysUser.created_at <= created_to)

        total = await self.db.scalar(select(func.count()).select_from(SysUser).where(*filters)) or 0
        rows = (await self.db.scalars(
            select(SysUser).where(*filters).order_by(SysUser.created_at.desc()).offset((page.page - 1) * page.page_size).limit(page.page_size)
        )).all()
        return PageResult(list=[await self._to_list_vo(row) for row in rows], total=str(total), page=page.page, pageSize=page.page_size)

    async def detail(self, user_id: int) -> UserDetailVo:
        user = await self._find_user(user_id)
        row = await self._to_list_vo(user)
        return UserDetailVo(**row.model_dump(by_alias=True), deptId=str(user.dept_id) if user.dept_id is not None else None, remark=user.remark)

    async def create(self, request: UserSaveRequest) -> str:
        await self._validate_save(request)
        existing = await self.db.scalar(select(SysUser.id).where(SysUser.username == request.username.strip()))
        if existing is not None:
            raise BusinessError(409000, "username already exists")
        password = request.password.strip() if request.password else "Admin@123456"
        if not 8 <= len(password) <= 64:
            raise bad_request("password length must be 8 to 64")
        salt = new_salt()
        user = SysUser(
            id=new_id(), username=request.username.strip(), real_name=request.real_name.strip(), phone=_optional(request.phone),
            email=_optional(request.email), status=1 if request.status is None else request.status, dept_id=request.dept_id,
            remark=_optional(request.remark), password_salt=salt, password_hash=hash_password(password, salt), deleted=0,
        )
        self.db.add(user)
        await self.db.commit()
        return str(user.id)

    async def update(self, user_id: int, request: UserSaveRequest) -> None:
        user = await self._find_user(user_id)
        await self._validate_save(request)
        duplicate = await self.db.scalar(select(SysUser.id).where(SysUser.username == request.username.strip(), SysUser.id != user_id))
        if duplicate is not None:
            raise BusinessError(409000, "username already exists")
        user.username = request.username.strip()
        user.real_name = request.real_name.strip()
        user.phone = _optional(request.phone)
        user.email = _optional(request.email)
        user.status = 1 if request.status is None else request.status
        user.dept_id = request.dept_id
        user.remark = _optional(request.remark)
        await self.db.commit()

    async def delete(self, current_user_id: int, user_id: int) -> None:
        if current_user_id == user_id:
            raise bad_request("operation failed")
        await self._assert_not_super_admin_target(current_user_id, user_id)
        user = await self._find_user(user_id)
        user.deleted = 1
        await self.db.commit()

    async def update_status(self, current_user_id: int, user_id: int, request: StatusUpdateRequest) -> None:
        if request.status not in {0, 1}:
            raise bad_request("status is invalid")
        if current_user_id == user_id and request.status != 1:
            raise bad_request("operation failed")
        await self._assert_not_super_admin_target(current_user_id, user_id)
        user = await self._find_user(user_id)
        user.status = request.status
        await self.db.commit()

    async def unlock(self, user_id: int) -> None:
        user = await self._find_user(user_id)
        await LoginSecurityService(self.db, self.redis).clear(user.username)

    async def assign_roles(self, current_user_id: int, user_id: int, request: RoleAssignRequest) -> None:
        await self._assert_not_super_admin_target(current_user_id, user_id)
        await self._find_user(user_id)
        role_ids = request.role_ids
        if len(role_ids) != len(set(role_ids)):
            raise bad_request("operation failed")
        if role_ids:
            count = await self.db.scalar(select(func.count()).select_from(SysRole).where(SysRole.id.in_(role_ids), SysRole.deleted == 0))
            if count != len(role_ids):
                raise bad_request("operation failed")
        existing = (await self.db.scalars(select(SysUserRole).where(SysUserRole.user_id == user_id))).all()
        for row in existing:
            await self.db.delete(row)
        for role_id in role_ids:
            self.db.add(SysUserRole(id=new_id(), user_id=user_id, role_id=role_id))
        await self.db.commit()

    async def reset_password(self, current_user_id: int, user_id: int, request: PasswordResetRequest) -> None:
        await self._assert_not_super_admin_target(current_user_id, user_id)
        user = await self._find_user(user_id)
        password = request.password.strip() if request.password else "Admin@123456"
        if not 8 <= len(password) <= 64:
            raise bad_request("password length must be 8 to 64")
        user.password_salt = new_salt()
        user.password_hash = hash_password(password, user.password_salt)
        await self.db.commit()

    async def _validate_save(self, request: UserSaveRequest) -> None:
        if not request.username.strip():
            raise bad_request("username is required")
        if not request.real_name.strip():
            raise bad_request("realName is required")
        if request.status is not None and request.status not in {0, 1}:
            raise bad_request("status is invalid")
        if request.dept_id is not None:
            dept = await self.db.scalar(select(SysDept.id).where(SysDept.id == request.dept_id, SysDept.deleted == 0))
            if dept is None:
                raise bad_request("\u90e8\u95e8\u4e0d\u5b58\u5728")

    async def _find_user(self, user_id: int) -> SysUser:
        user = await self.db.scalar(select(SysUser).where(SysUser.id == user_id, SysUser.deleted == 0))
        if user is None:
            raise not_found()
        return user

    async def _assert_not_super_admin_target(self, current_user_id: int, target_user_id: int) -> None:
        current_roles = await self.permissions.role_codes(current_user_id)
        if "SUPER_ADMIN" in current_roles:
            return
        if "SUPER_ADMIN" in await self.permissions.role_codes(target_user_id):
            raise forbidden("\u4e0d\u80fd\u64cd\u4f5c\u8d85\u7ea7\u7ba1\u7406\u5458")

    async def _to_list_vo(self, user: SysUser) -> UserListVo:
        dept = None
        if user.dept_id is not None:
            row = await self.db.scalar(select(SysDept).where(SysDept.id == user.dept_id, SysDept.deleted == 0))
            if row is not None:
                dept = DeptSummaryVo(id=str(row.id), deptName=row.dept_name)
        query = select(SysRole).join(SysUserRole, SysUserRole.role_id == SysRole.id).where(
            SysUserRole.user_id == user.id, SysRole.deleted == 0
        )
        roles = [RoleSummaryVo(id=str(row.id), roleName=row.role_name, roleCode=row.role_code) for row in (await self.db.scalars(query)).all()]
        return UserListVo(
            id=str(user.id), username=user.username, realName=user.real_name, phone=user.phone, email=user.email,
            status=user.status, dept=dept, roles=roles, createdAt=user.created_at, lastLoginAt=user.last_login_at,
        )


def _optional(value: str | None) -> str | None:
    return value.strip() if value and value.strip() else None
