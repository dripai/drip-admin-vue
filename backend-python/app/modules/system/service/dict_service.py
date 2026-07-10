from redis.asyncio import Redis
from sqlalchemy import func, select, update
from sqlalchemy.ext.asyncio import AsyncSession

from app.common.errors import BusinessError, bad_request, not_found
from app.common.id import new_id
from app.modules.system.dto.dict_request import DictItemSaveRequest, DictTypeSaveRequest
from app.modules.system.entity import SysDictItem, SysDictType


class DictService:
    def __init__(self, db: AsyncSession, redis: Redis) -> None:
        self.db = db
        self.redis = redis

    async def types(self) -> list[dict]:
        rows = (await self.db.scalars(select(SysDictType).order_by(SysDictType.created_at.desc()))).all()
        return [self._type_vo(row) for row in rows]

    async def items(self, dict_type_id: int) -> list[dict]:
        if await self.db.scalar(select(SysDictType.id).where(SysDictType.id == dict_type_id)) is None:
            raise not_found()
        rows = (
            await self.db.scalars(
                select(SysDictItem)
                .where(SysDictItem.dict_type_id == dict_type_id)
                .order_by(SysDictItem.sort, SysDictItem.id)
            )
        ).all()
        return [self._item_vo(row) for row in rows]

    async def save_type(self, dict_type_id: int | None, request: DictTypeSaveRequest) -> str | None:
        name = request.dict_name.strip()
        code = request.dict_code.strip()
        duplicate = select(SysDictType.id).where(SysDictType.dict_code == code)
        if dict_type_id is not None:
            duplicate = duplicate.where(SysDictType.id != dict_type_id)
        if await self.db.scalar(duplicate) is not None:
            raise BusinessError(409000, "dictCode already exists")
        if dict_type_id is None:
            row = SysDictType(
                id=new_id(),
                dict_name=name,
                dict_code=code,
                status=1 if request.status is None else request.status,
                builtin=0 if request.builtin is None else request.builtin,
                remark=_optional(request.remark),
            )
            self.db.add(row)
            await self.db.commit()
            return str(row.id)
        row = await self._find_type(dict_type_id)
        row.dict_name = name
        row.dict_code = code
        row.status = 1 if request.status is None else request.status
        row.remark = _optional(request.remark)
        if row.builtin != 1:
            row.builtin = 0 if request.builtin is None else request.builtin
        await self.db.commit()
        return None

    async def delete_type(self, dict_type_id: int) -> None:
        row = await self._find_type(dict_type_id)
        if row.builtin == 1:
            raise bad_request("built-in dictionary type cannot be deleted")
        count = await self.db.scalar(
            select(func.count()).select_from(SysDictItem).where(SysDictItem.dict_type_id == dict_type_id)
        )
        if count:
            raise BusinessError(409000, "dictionary type contains items")
        await self.db.delete(row)
        await self.db.commit()

    async def save_item(self, item_id: int | None, request: DictItemSaveRequest) -> str | None:
        if await self.db.scalar(select(SysDictType.id).where(SysDictType.id == request.dict_type_id)) is None:
            raise not_found()
        duplicate = select(SysDictItem.id).where(
            SysDictItem.dict_type_id == request.dict_type_id,
            SysDictItem.value == request.value.strip(),
        )
        if item_id is not None:
            duplicate = duplicate.where(SysDictItem.id != item_id)
        if await self.db.scalar(duplicate) is not None:
            raise BusinessError(409000, "dictionary item value already exists")

        is_default = 0 if request.is_default is None else request.is_default
        if is_default == 1:
            statement = update(SysDictItem).where(
                SysDictItem.dict_type_id == request.dict_type_id,
                SysDictItem.is_default == 1,
            )
            if item_id is not None:
                statement = statement.where(SysDictItem.id != item_id)
            await self.db.execute(statement.values(is_default=0))

        if item_id is None:
            row = SysDictItem(
                id=new_id(),
                dict_type_id=request.dict_type_id,
                label=request.label.strip(),
                value=request.value.strip(),
                is_default=is_default,
                sort=0 if request.sort is None else request.sort,
                status=1 if request.status is None else request.status,
                builtin=0 if request.builtin is None else request.builtin,
            )
            self.db.add(row)
            await self.db.commit()
            return str(row.id)
        row = await self._find_item(item_id)
        row.dict_type_id = request.dict_type_id
        row.label = request.label.strip()
        row.value = request.value.strip()
        row.is_default = is_default
        row.sort = 0 if request.sort is None else request.sort
        row.status = 1 if request.status is None else request.status
        if row.builtin != 1:
            row.builtin = 0 if request.builtin is None else request.builtin
        await self.db.commit()
        return None

    async def delete_item(self, item_id: int) -> None:
        row = await self._find_item(item_id)
        if row.builtin == 1:
            raise bad_request("built-in dictionary item cannot be deleted")
        await self.db.delete(row)
        await self.db.commit()

    async def update_item_status(self, item_id: int, status: int | None) -> None:
        row = await self._find_item(item_id)
        row.status = 1 if status is None else status
        await self.db.commit()

    async def refresh_cache(self) -> None:
        keys = [key async for key in self.redis.scan_iter(match="drip:dict:*")]
        if keys:
            await self.redis.delete(*keys)

    async def _find_type(self, dict_type_id: int) -> SysDictType:
        row = await self.db.scalar(select(SysDictType).where(SysDictType.id == dict_type_id))
        if row is None:
            raise not_found()
        return row

    async def _find_item(self, item_id: int) -> SysDictItem:
        row = await self.db.scalar(select(SysDictItem).where(SysDictItem.id == item_id))
        if row is None:
            raise not_found()
        return row

    @staticmethod
    def _type_vo(row: SysDictType) -> dict:
        return {
            "id": str(row.id),
            "dictName": row.dict_name,
            "dictCode": row.dict_code,
            "status": row.status,
            "builtin": row.builtin,
            "remark": row.remark,
            "createdAt": row.created_at,
            "updatedAt": row.updated_at,
        }

    @staticmethod
    def _item_vo(row: SysDictItem) -> dict:
        return {
            "id": str(row.id),
            "dictTypeId": str(row.dict_type_id),
            "label": row.label,
            "value": row.value,
            "isDefault": row.is_default,
            "sort": row.sort,
            "status": row.status,
            "builtin": row.builtin,
            "createdAt": row.created_at,
            "updatedAt": row.updated_at,
        }


def _optional(value: str | None) -> str | None:
    return value.strip() if value and value.strip() else None
