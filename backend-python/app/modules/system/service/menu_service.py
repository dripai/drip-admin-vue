from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.modules.system.entity import SysMenu, SysRole, SysRoleMenu, SysUserRole
from app.modules.system.service.permission_service import PermissionService
from app.common.errors import bad_request, not_found
from app.common.id import new_id
from app.modules.system.dto.menu_request import MenuSaveRequest


class MenuService:
    def __init__(self, db: AsyncSession) -> None:
        self.db = db
        self.permissions = PermissionService(db)

    async def menu_tree_for_user(self, user_id: int) -> list[dict]:
        roles = await self.permissions.role_codes(user_id)
        if "SUPER_ADMIN" in roles:
            query = select(SysMenu).where(SysMenu.status == 1, SysMenu.deleted == 0)
        else:
            query = (
                select(SysMenu)
                .join(SysRoleMenu, SysRoleMenu.menu_id == SysMenu.id)
                .join(SysRole, SysRole.id == SysRoleMenu.role_id)
                .join(SysUserRole, SysUserRole.role_id == SysRole.id)
                .where(
                    SysUserRole.user_id == user_id,
                    SysRole.status == 1,
                    SysRole.deleted == 0,
                    SysMenu.status == 1,
                    SysMenu.deleted == 0,
                )
            )
        rows = {row.id: row for row in (await self.db.scalars(query)).all()}
        nodes = {menu_id: self._node(row) for menu_id, row in rows.items()}
        roots: list[dict] = []
        for menu_id, row in sorted(rows.items(), key=lambda item: (item[1].sort, item[0])):
            node = nodes[menu_id]
            parent = nodes.get(row.parent_id)
            if parent is None:
                roots.append(node)
            else:
                parent["children"].append(node)
        return roots

    async def all_tree(self) -> list[dict]:
        rows = (await self.db.scalars(select(SysMenu).where(SysMenu.deleted == 0).order_by(SysMenu.sort, SysMenu.id))).all()
        return self._tree(rows, False)

    async def save(self, menu_id: int | None, body: MenuSaveRequest) -> str | None:
        if not body.name.strip() or not body.type.strip(): raise bad_request("name and type are required")
        if body.visible is not None and body.visible not in {0, 1}: raise bad_request("visible is invalid")
        if body.status is not None and body.status not in {0, 1}: raise bad_request("status is invalid")
        parent_id = body.parent_id or 0
        if parent_id:
            if not await self.db.scalar(select(SysMenu.id).where(SysMenu.id == parent_id, SysMenu.deleted == 0)): raise not_found()
            if menu_id == parent_id: raise bad_request("operation failed")
        values = dict(parent_id=parent_id, name=body.name.strip(), type=body.type.strip(), path=body.path or None, component=body.component or None, permission_code=body.permission_code or None, icon=body.icon or None, sort=body.sort or 0, visible=1 if body.visible is None else body.visible, status=1 if body.status is None else body.status)
        if menu_id is None:
            row = SysMenu(id=new_id(), deleted=0, **values); self.db.add(row); await self.db.commit(); return str(row.id)
        row = await self.db.scalar(select(SysMenu).where(SysMenu.id == menu_id, SysMenu.deleted == 0))
        if not row: raise not_found()
        for key, value in values.items(): setattr(row, key, value)
        await self.db.commit(); return None

    async def delete(self, menu_id: int) -> None:
        row = await self.db.scalar(select(SysMenu).where(SysMenu.id == menu_id, SysMenu.deleted == 0))
        if not row: raise not_found()
        if await self.db.scalar(select(func.count()).select_from(SysMenu).where(SysMenu.parent_id == menu_id, SysMenu.deleted == 0)): raise bad_request("operation failed")
        row.deleted = 1; await self.db.commit()

    async def update_status(self, menu_id: int, status: int) -> None:
        if status not in {0, 1}: raise bad_request("status is invalid")
        row = await self.db.scalar(select(SysMenu).where(SysMenu.id == menu_id, SysMenu.deleted == 0))
        if not row: raise not_found()
        row.status = status; await self.db.commit()

    def _tree(self, rows: list[SysMenu], exclude_button: bool) -> list[dict]:
        nodes = {row.id: self._node(row) for row in rows if not exclude_button or row.type != "BUTTON"}
        roots = []
        for row in rows:
            node = nodes.get(row.id)
            if not node: continue
            (nodes[row.parent_id]["children"] if row.parent_id in nodes else roots).append(node)
        return roots

    @staticmethod
    def _node(menu: SysMenu) -> dict:
        return {
            "id": str(menu.id),
            "parentId": str(menu.parent_id),
            "name": menu.name,
            "type": menu.type,
            "path": menu.path,
            "component": menu.component,
            "permissionCode": menu.permission_code,
            "icon": menu.icon,
            "sort": menu.sort,
            "visible": menu.visible,
            "children": [],
        }
