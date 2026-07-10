from __future__ import annotations

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.common.errors import BusinessError, bad_request, not_found
from app.common.id import new_id
from app.common.pagination import PageQuery, PageResult
from app.modules.system.dto.role_request import MenuAssignRequest, RoleSaveRequest
from app.modules.system.dto.user_request import StatusUpdateRequest
from app.modules.system.entity import SysMenu, SysRole, SysRoleMenu, SysUser, SysUserRole


class RoleService:
    def __init__(self, db: AsyncSession) -> None:
        self.db = db

    async def list(self, page: PageQuery, role_name: str | None, role_code: str | None, status: int | None) -> PageResult[dict]:
        where = [SysRole.deleted == 0]
        if role_name: where.append(SysRole.role_name.like(f"%{role_name}%"))
        if role_code: where.append(SysRole.role_code.like(f"%{role_code}%"))
        if status is not None: where.append(SysRole.status == status)
        total = await self.db.scalar(select(func.count()).select_from(SysRole).where(*where)) or 0
        rows = (await self.db.scalars(select(SysRole).where(*where).order_by(SysRole.created_at.desc()).offset((page.page-1)*page.page_size).limit(page.page_size))).all()
        return PageResult(list=[self._vo(row) for row in rows], total=str(total), page=page.page, pageSize=page.page_size)

    async def detail(self, role_id: int) -> dict:
        return self._vo(await self._find(role_id))

    async def options(self) -> list[dict]:
        return [self._vo(row) for row in (await self.db.scalars(select(SysRole).where(SysRole.deleted == 0))).all()]

    async def permissions(self, role_id: int) -> dict:
        await self._find(role_id)
        ids = (await self.db.scalars(select(SysRoleMenu.menu_id).where(SysRoleMenu.role_id == role_id))).all()
        return {"menuIds": [str(item) for item in ids], "permissionCode": []}

    async def create(self, body: RoleSaveRequest) -> str:
        await self._validate(body)
        if await self.db.scalar(select(SysRole.id).where(SysRole.role_code == body.role_code.strip())): raise BusinessError(409000, "roleCode already exists")
        row = SysRole(id=new_id(), role_name=body.role_name.strip(), role_code=body.role_code.strip(), builtin=0, status=body.status if body.status is not None else 1, remark=body.remark, deleted=0)
        self.db.add(row); await self.db.commit(); return str(row.id)

    async def update(self, role_id: int, body: RoleSaveRequest) -> None:
        row = await self._find(role_id); await self._validate(body)
        duplicate = await self.db.scalar(select(SysRole.id).where(SysRole.role_code == body.role_code.strip(), SysRole.id != role_id))
        if duplicate: raise BusinessError(409000, "roleCode already exists")
        row.role_name, row.role_code, row.status, row.remark = body.role_name.strip(), body.role_code.strip(), body.status if body.status is not None else 1, body.remark
        await self.db.commit()

    async def delete(self, role_id: int) -> None:
        row = await self._find(role_id)
        if row.builtin == 1: raise bad_request("operation failed")
        if await self.db.scalar(select(func.count()).select_from(SysUserRole).where(SysUserRole.role_id == role_id)): raise BusinessError(409000, "operation failed")
        row.deleted = 1; await self.db.commit()

    async def update_status(self, role_id: int, status: int) -> None:
        if status not in {0, 1}: raise bad_request("status is invalid")
        row = await self._find(role_id); row.status = status; await self.db.commit()

    async def assign_menus(self, role_id: int, body: MenuAssignRequest) -> None:
        await self._find(role_id)
        if len(body.menu_ids) != len(set(body.menu_ids)): raise bad_request("operation failed")
        if body.menu_ids:
            count = await self.db.scalar(select(func.count()).select_from(SysMenu).where(SysMenu.id.in_(body.menu_ids), SysMenu.deleted == 0))
            if count != len(body.menu_ids): raise bad_request("operation failed")
        for row in (await self.db.scalars(select(SysRoleMenu).where(SysRoleMenu.role_id == role_id))).all(): await self.db.delete(row)
        for menu_id in body.menu_ids: self.db.add(SysRoleMenu(id=new_id(), role_id=role_id, menu_id=menu_id))
        await self.db.commit()

    async def users(self, role_id: int, page: PageQuery) -> PageResult[dict]:
        await self._find(role_id)
        where = [SysUser.deleted == 0, SysUser.id.in_(select(SysUserRole.user_id).where(SysUserRole.role_id == role_id))]
        total = await self.db.scalar(select(func.count()).select_from(SysUser).where(*where)) or 0
        rows = (await self.db.scalars(select(SysUser).where(*where).offset((page.page-1)*page.page_size).limit(page.page_size))).all()
        return PageResult(list=[{"id":str(r.id),"username":r.username,"realName":r.real_name,"status":r.status} for r in rows], total=str(total), page=page.page, pageSize=page.page_size)

    async def _find(self, role_id: int) -> SysRole:
        row = await self.db.scalar(select(SysRole).where(SysRole.id == role_id, SysRole.deleted == 0))
        if not row: raise not_found()
        return row

    async def _validate(self, body: RoleSaveRequest) -> None:
        if not body.role_name.strip(): raise bad_request("roleName is required")
        if not body.role_code.strip(): raise bad_request("roleCode is required")
        if body.status is not None and body.status not in {0, 1}: raise bad_request("status is invalid")

    @staticmethod
    def _vo(row: SysRole) -> dict:
        return {"id": str(row.id), "roleName": row.role_name, "roleCode": row.role_code, "builtin": row.builtin, "status": row.status, "remark": row.remark, "createdAt": row.created_at, "updatedAt": row.updated_at}
