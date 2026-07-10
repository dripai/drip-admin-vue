from __future__ import annotations

from sqlalchemy import String, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.common.errors import BusinessError, bad_request, not_found
from app.common.id import new_id
from app.common.pagination import PageQuery, PageResult
from app.modules.system.dto.role_request import MenuAssignRequest, RoleSaveRequest
from app.modules.system.entity import SysMenu, SysRole, SysRoleMenu, SysUser, SysUserRole


class RoleService:
    def __init__(self, db: AsyncSession) -> None:
        self.db = db

    async def list_roles(
        self,
        page: PageQuery,
        role_name: str | None,
        role_code: str | None,
        status: int | None,
        created_at: str | None,
    ) -> PageResult[dict]:
        filters = [SysRole.deleted == 0]
        if role_name and role_name.strip():
            filters.append(SysRole.role_name.like(f"%{role_name.strip()}%"))
        if role_code and role_code.strip():
            filters.append(SysRole.role_code.like(f"%{role_code.strip()}%"))
        if status is not None:
            filters.append(SysRole.status == status)
        if created_at and created_at.strip():
            filters.append(SysRole.created_at.cast(String).like(f"%{created_at.strip()}%"))
        total = await self.db.scalar(
            select(func.count()).select_from(SysRole).where(*filters)
        ) or 0
        rows = (
            await self.db.scalars(
                select(SysRole)
                .where(*filters)
                .order_by(SysRole.created_at.desc(), SysRole.id.desc())
                .offset((page.page - 1) * page.page_size)
                .limit(page.page_size)
            )
        ).all()
        return PageResult(
            list=[self._vo(row) for row in rows],
            total=str(total),
            page=page.page,
            pageSize=page.page_size,
        )

    async def detail(self, role_id: int) -> dict:
        return self._vo(await self._find(role_id))

    async def options(self) -> list[dict]:
        rows = (
            await self.db.scalars(
                select(SysRole).where(SysRole.deleted == 0).order_by(SysRole.id)
            )
        ).all()
        return [self._vo(row) for row in rows]

    async def permissions(self, role_id: int) -> dict:
        await self._find(role_id)
        menu_ids = (
            await self.db.scalars(
                select(SysRoleMenu.menu_id)
                .where(SysRoleMenu.role_id == role_id)
                .order_by(SysRoleMenu.menu_id)
            )
        ).all()
        return {
            "menuIds": [str(menu_id) for menu_id in menu_ids],
            "permissionCodes": [],
        }

    async def create(self, request: RoleSaveRequest) -> str:
        await self._assert_role_code_available(request.role_code.strip(), None)
        row = SysRole(
            id=new_id(),
            role_name=request.role_name.strip(),
            role_code=request.role_code.strip(),
            builtin=0,
            status=1 if request.status is None else request.status,
            remark=_optional(request.remark),
            deleted=0,
        )
        self.db.add(row)
        await self.db.commit()
        return str(row.id)

    async def update(self, role_id: int, request: RoleSaveRequest) -> None:
        row = await self._find(role_id)
        await self._assert_role_code_available(request.role_code.strip(), role_id)
        row.role_name = request.role_name.strip()
        row.role_code = request.role_code.strip()
        row.status = 1 if request.status is None else request.status
        row.remark = _optional(request.remark)
        await self.db.commit()

    async def delete(self, role_id: int) -> None:
        row = await self._find(role_id)
        if row.builtin == 1:
            raise bad_request("operation failed")
        count = await self.db.scalar(
            select(func.count()).select_from(SysUserRole).where(SysUserRole.role_id == role_id)
        )
        if count:
            raise BusinessError(409000, "operation failed")
        row.deleted = 1
        await self.db.commit()

    async def update_status(self, role_id: int, status: int | None) -> None:
        row = await self._find(role_id)
        row.status = 1 if status is None else status
        await self.db.commit()

    async def assign_menus(self, role_id: int, request: MenuAssignRequest) -> None:
        await self._find(role_id)
        if len(request.menu_ids) != len(set(request.menu_ids)):
            raise bad_request("operation failed")
        if request.menu_ids:
            count = await self.db.scalar(
                select(func.count()).select_from(SysMenu).where(
                    SysMenu.id.in_(request.menu_ids),
                    SysMenu.deleted == 0,
                )
            )
            if count != len(request.menu_ids):
                raise bad_request("operation failed")
        await self.db.execute(delete(SysRoleMenu).where(SysRoleMenu.role_id == role_id))
        self.db.add_all(
            SysRoleMenu(id=new_id(), role_id=role_id, menu_id=menu_id)
            for menu_id in request.menu_ids
        )
        await self.db.commit()

    async def users(self, role_id: int, page: PageQuery) -> PageResult[dict]:
        await self._find(role_id)
        filters = [
            SysUser.deleted == 0,
            SysUser.id.in_(
                select(SysUserRole.user_id).where(SysUserRole.role_id == role_id)
            ),
        ]
        total = await self.db.scalar(
            select(func.count()).select_from(SysUser).where(*filters)
        ) or 0
        rows = (
            await self.db.scalars(
                select(SysUser)
                .where(*filters)
                .order_by(SysUser.created_at.desc(), SysUser.id.desc())
                .offset((page.page - 1) * page.page_size)
                .limit(page.page_size)
            )
        ).all()
        return PageResult(
            list=[self._user_vo(row) for row in rows],
            total=str(total),
            page=page.page,
            pageSize=page.page_size,
        )

    async def _find(self, role_id: int) -> SysRole:
        row = await self.db.scalar(
            select(SysRole).where(SysRole.id == role_id, SysRole.deleted == 0)
        )
        if row is None:
            raise not_found()
        return row

    async def _assert_role_code_available(self, role_code: str, role_id: int | None) -> None:
        query = select(SysRole.id).where(
            SysRole.role_code == role_code,
            SysRole.deleted == 0,
        )
        if role_id is not None:
            query = query.where(SysRole.id != role_id)
        if await self.db.scalar(query) is not None:
            raise BusinessError(409000, "roleCode already exists")

    @staticmethod
    def _vo(row: SysRole) -> dict:
        return {
            "id": str(row.id),
            "roleName": row.role_name,
            "roleCode": row.role_code,
            "builtin": row.builtin,
            "status": row.status,
            "remark": row.remark,
            "createdAt": row.created_at,
            "updatedAt": row.updated_at,
        }

    @staticmethod
    def _user_vo(row: SysUser) -> dict:
        return {
            "id": str(row.id),
            "username": row.username,
            "realName": row.real_name,
            "phone": row.phone,
            "email": row.email,
            "avatar": row.avatar,
            "status": row.status,
            "deptId": str(row.dept_id) if row.dept_id is not None else None,
            "remark": row.remark,
            "lastLoginAt": row.last_login_at,
            "createdAt": row.created_at,
            "updatedAt": row.updated_at,
        }


def _optional(value: str | None) -> str | None:
    return value.strip() if value and value.strip() else None
