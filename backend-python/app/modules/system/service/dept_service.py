from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.common.errors import BusinessError, bad_request, not_found
from app.common.id import new_id
from app.modules.system.dto.dept_request import DeptSaveRequest
from app.modules.system.entity import SysDept, SysUser


class DeptService:
    def __init__(self, db: AsyncSession) -> None:
        self.db = db

    async def tree(self) -> list[dict]:
        rows = list(
            (
                await self.db.scalars(
                    select(SysDept)
                    .where(SysDept.deleted == 0)
                    .order_by(SysDept.sort, SysDept.id)
                )
            ).all()
        )
        nodes = {row.id: self._vo(row) for row in rows}
        roots: list[dict] = []
        for row in rows:
            parent = nodes.get(row.parent_id)
            if parent is None:
                roots.append(nodes[row.id])
            else:
                parent["children"].append(nodes[row.id])
        return roots

    async def detail(self, dept_id: int) -> dict:
        return self._vo(await self._find(dept_id))

    async def save(self, dept_id: int | None, request: DeptSaveRequest) -> str | None:
        parent_id = request.parent_id or 0
        await self._assert_valid_parent(dept_id, parent_id)
        await self._assert_leader(request.leader_user_id)
        duplicate = select(SysDept.id).where(
            SysDept.dept_code == request.dept_code.strip(),
            SysDept.deleted == 0,
        )
        if dept_id is not None:
            duplicate = duplicate.where(SysDept.id != dept_id)
        if await self.db.scalar(duplicate) is not None:
            raise BusinessError(409000, "deptCode already exists")
        values = {
            "parent_id": parent_id,
            "dept_name": request.dept_name.strip(),
            "dept_code": request.dept_code.strip(),
            "leader_user_id": request.leader_user_id,
            "sort": 0 if request.sort is None else request.sort,
            "status": 1 if request.status is None else request.status,
        }
        if dept_id is None:
            row = SysDept(id=new_id(), deleted=0, **values)
            self.db.add(row)
            await self.db.commit()
            return str(row.id)
        row = await self._find(dept_id)
        for key, value in values.items():
            setattr(row, key, value)
        await self.db.commit()
        return None

    async def delete(self, dept_id: int) -> None:
        row = await self._find(dept_id)
        child_count = await self.db.scalar(
            select(func.count()).select_from(SysDept).where(
                SysDept.parent_id == dept_id,
                SysDept.deleted == 0,
            )
        )
        if child_count:
            raise BusinessError(409000, "department contains child departments")
        user_count = await self.db.scalar(
            select(func.count()).select_from(SysUser).where(
                SysUser.dept_id == dept_id,
                SysUser.deleted == 0,
            )
        )
        if user_count:
            raise BusinessError(409000, "department contains users")
        row.deleted = 1
        await self.db.commit()

    async def update_status(self, dept_id: int, status: int | None) -> None:
        row = await self._find(dept_id)
        row.status = 1 if status is None else status
        await self.db.commit()

    async def _assert_valid_parent(self, dept_id: int | None, parent_id: int) -> None:
        if parent_id == 0:
            return
        await self._find(parent_id)
        if dept_id is None:
            return
        if parent_id == dept_id:
            raise bad_request("parent department cannot be self or descendant")
        rows = (await self.db.execute(select(SysDept.id, SysDept.parent_id).where(SysDept.deleted == 0))).all()
        if parent_id in _descendants(dept_id, rows):
            raise bad_request("parent department cannot be self or descendant")

    async def _assert_leader(self, user_id: int | None) -> None:
        if user_id is None:
            return
        exists = await self.db.scalar(
            select(SysUser.id).where(SysUser.id == user_id, SysUser.deleted == 0)
        )
        if exists is None:
            raise bad_request("leaderUserId is invalid")

    async def _find(self, dept_id: int) -> SysDept:
        row = await self.db.scalar(select(SysDept).where(SysDept.id == dept_id, SysDept.deleted == 0))
        if row is None:
            raise not_found()
        return row

    @staticmethod
    def _vo(row: SysDept) -> dict:
        return {
            "id": str(row.id),
            "parentId": str(row.parent_id),
            "deptName": row.dept_name,
            "deptCode": row.dept_code,
            "leaderUserId": str(row.leader_user_id) if row.leader_user_id is not None else None,
            "sort": row.sort,
            "status": row.status,
            "createdAt": row.created_at,
            "updatedAt": row.updated_at,
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
