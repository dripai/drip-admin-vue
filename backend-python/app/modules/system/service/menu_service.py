from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.common.errors import BusinessError, bad_request, not_found
from app.common.id import new_id
from app.modules.system.dto.menu_request import MenuSaveRequest
from app.modules.system.entity import SysMenu, SysRole, SysRoleMenu, SysUserRole
from app.modules.system.service.permission_service import PermissionService


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
        rows = list((await self.db.scalars(query)).unique().all())
        return self._tree(rows)

    async def all_tree(self) -> list[dict]:
        rows = list(
            (
                await self.db.scalars(
                    select(SysMenu)
                    .where(SysMenu.deleted == 0)
                    .order_by(SysMenu.sort, SysMenu.id)
                )
            ).all()
        )
        return self._tree(rows)

    async def save(self, menu_id: int | None, request: MenuSaveRequest) -> str | None:
        parent_id = request.parent_id or 0
        await self._assert_valid_parent(menu_id, parent_id)
        permission_code = _optional(request.permission_code)
        if permission_code:
            duplicate = select(SysMenu.id).where(
                SysMenu.permission_code == permission_code,
                SysMenu.deleted == 0,
            )
            if menu_id is not None:
                duplicate = duplicate.where(SysMenu.id != menu_id)
            if await self.db.scalar(duplicate) is not None:
                raise BusinessError(409000, "permissionCode already exists")
        values = {
            "parent_id": parent_id,
            "name": request.name.strip(),
            "type": request.type.strip(),
            "path": _optional(request.path),
            "component": _optional(request.component),
            "permission_code": permission_code,
            "icon": _optional(request.icon),
            "sort": 0 if request.sort is None else request.sort,
            "visible": 1 if request.visible is None else request.visible,
            "status": 1 if request.status is None else request.status,
        }
        if menu_id is None:
            row = SysMenu(id=new_id(), deleted=0, **values)
            self.db.add(row)
            await self.db.commit()
            return str(row.id)
        row = await self._find(menu_id)
        for key, value in values.items():
            setattr(row, key, value)
        await self.db.commit()
        return None

    async def delete(self, menu_id: int) -> None:
        row = await self._find(menu_id)
        child_count = await self.db.scalar(
            select(func.count()).select_from(SysMenu).where(
                SysMenu.parent_id == menu_id,
                SysMenu.deleted == 0,
            )
        )
        if child_count:
            raise BusinessError(409000, "menu contains child nodes")
        row.deleted = 1
        await self.db.commit()

    async def update_status(self, menu_id: int, status: int | None) -> None:
        row = await self._find(menu_id)
        row.status = 1 if status is None else status
        await self.db.commit()

    async def _assert_valid_parent(self, menu_id: int | None, parent_id: int) -> None:
        if parent_id == 0:
            return
        await self._find(parent_id)
        if menu_id is None:
            return
        if parent_id == menu_id:
            raise bad_request("operation failed")
        rows = (await self.db.execute(select(SysMenu.id, SysMenu.parent_id).where(SysMenu.deleted == 0))).all()
        descendants = _descendants(menu_id, rows)
        if parent_id in descendants:
            raise bad_request("operation failed")

    async def _find(self, menu_id: int) -> SysMenu:
        row = await self.db.scalar(select(SysMenu).where(SysMenu.id == menu_id, SysMenu.deleted == 0))
        if row is None:
            raise not_found()
        return row

    def _tree(self, rows: list[SysMenu]) -> list[dict]:
        ordered = sorted(rows, key=lambda row: (row.sort, row.id))
        nodes = {row.id: self._node(row) for row in ordered}
        roots: list[dict] = []
        for row in ordered:
            node = nodes[row.id]
            parent = nodes.get(row.parent_id)
            if parent is None:
                roots.append(node)
            else:
                parent["children"].append(node)
        return roots

    @staticmethod
    def _node(row: SysMenu) -> dict:
        return {
            "id": str(row.id),
            "parentId": str(row.parent_id),
            "name": row.name,
            "type": row.type,
            "path": row.path,
            "component": row.component,
            "permissionCode": row.permission_code,
            "icon": row.icon,
            "sort": row.sort,
            "visible": row.visible,
            "status": row.status,
            "children": [],
        }


def _descendants(parent_id: int, rows: list[tuple[int, int]]) -> set[int]:
    children: dict[int, list[int]] = {}
    for child_id, current_parent_id in rows:
        children.setdefault(current_parent_id, []).append(child_id)
    result: set[int] = set()
    stack = list(children.get(parent_id, []))
    while stack:
        current = stack.pop()
        if current in result:
            continue
        result.add(current)
        stack.extend(children.get(current, []))
    return result


def _optional(value: str | None) -> str | None:
    return value.strip() if value and value.strip() else None
