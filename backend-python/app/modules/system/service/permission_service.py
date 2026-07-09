from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.modules.system.entity import SysMenu, SysRole, SysRoleMenu, SysUserRole


class PermissionService:
    def __init__(self, db: AsyncSession) -> None:
        self.db = db

    async def role_codes(self, user_id: int) -> list[str]:
        query = (
            select(SysRole.role_code)
            .join(SysUserRole, SysUserRole.role_id == SysRole.id)
            .where(SysUserRole.user_id == user_id, SysRole.status == 1, SysRole.deleted == 0)
        )
        return list((await self.db.scalars(query)).all())

    async def permission_codes(self, user_id: int) -> list[str]:
        roles = await self.role_codes(user_id)
        if "SUPER_ADMIN" in roles:
            query = select(SysMenu.permission_code).where(
                SysMenu.status == 1, SysMenu.deleted == 0, SysMenu.permission_code.is_not(None)
            )
        else:
            query = (
                select(SysMenu.permission_code)
                .join(SysRoleMenu, SysRoleMenu.menu_id == SysMenu.id)
                .join(SysRole, SysRole.id == SysRoleMenu.role_id)
                .join(SysUserRole, SysUserRole.role_id == SysRole.id)
                .where(
                    SysUserRole.user_id == user_id,
                    SysRole.status == 1,
                    SysRole.deleted == 0,
                    SysMenu.status == 1,
                    SysMenu.deleted == 0,
                    SysMenu.permission_code.is_not(None),
                )
            )
        return sorted(set((await self.db.scalars(query)).all()))

