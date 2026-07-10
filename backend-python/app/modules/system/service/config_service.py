from decimal import Decimal, InvalidOperation

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.common.errors import BusinessError, bad_request, not_found
from app.common.id import new_id
from app.common.pagination import PageQuery, PageResult
from app.modules.system.dto.config_request import ConfigSaveRequest
from app.modules.system.entity import SysConfig


class ConfigService:
    def __init__(self, db: AsyncSession) -> None:
        self.db = db

    async def list_configs(
        self,
        page: PageQuery,
        config_name: str | None,
        config_key: str | None,
        status: int | None,
    ) -> PageResult[dict]:
        filters = [SysConfig.deleted == 0]
        if config_name and config_name.strip():
            filters.append(SysConfig.config_name.like(f"%{config_name.strip()}%"))
        if config_key and config_key.strip():
            filters.append(SysConfig.config_key.like(f"%{config_key.strip()}%"))
        if status is not None:
            filters.append(SysConfig.status == status)
        total = await self.db.scalar(select(func.count()).select_from(SysConfig).where(*filters)) or 0
        rows = (
            await self.db.scalars(
                select(SysConfig)
                .where(*filters)
                .order_by(SysConfig.created_at.desc(), SysConfig.id.desc())
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

    async def public_configs(self) -> dict[str, str]:
        return {
            "systemName": await self.required_config("system.name"),
            "companyFullName": await self.config_or_default("system.company.fullName", ""),
            "logoUrl": await self.config_or_default("system.logo", ""),
            "watermarkEnabled": await self.config_or_default("system.watermark.enabled", "false"),
            "silentPrintEnabled": await self.config_or_default("print.silent.enabled", "false"),
        }

    async def save(self, config_id: int | None, request: ConfigSaveRequest) -> str | None:
        name = request.config_name.strip()
        key = request.config_key.strip()
        self._validate_value(request.value_type, request.config_value)
        duplicate_query = select(SysConfig.id).where(SysConfig.config_key == key, SysConfig.deleted == 0)
        if config_id is not None:
            duplicate_query = duplicate_query.where(SysConfig.id != config_id)
        if await self.db.scalar(duplicate_query) is not None:
            raise BusinessError(409000, "configKey already exists")

        if config_id is None:
            row = SysConfig(
                id=new_id(),
                config_name=name,
                config_key=key,
                config_value=request.config_value,
                value_type=request.value_type,
                builtin=0,
                status=1 if request.status is None else request.status,
                remark=_optional(request.remark),
                deleted=0,
            )
            self.db.add(row)
            await self.db.commit()
            return str(row.id)

        row = await self._find(config_id)
        row.config_name = name
        row.config_key = key
        row.config_value = request.config_value
        row.value_type = request.value_type
        row.remark = _optional(request.remark)
        if row.builtin != 1:
            row.status = 1 if request.status is None else request.status
        await self.db.commit()
        return None

    async def delete(self, config_id: int) -> None:
        row = await self._find(config_id)
        if row.builtin == 1:
            raise bad_request("operation failed")
        row.deleted = 1
        await self.db.commit()

    async def update_status(self, config_id: int, status: int | None) -> None:
        row = await self._find(config_id)
        if row.builtin == 1:
            raise bad_request("operation failed")
        row.status = 1 if status is None else status
        await self.db.commit()

    async def required_config(self, key: str) -> str:
        value = await self._config_value(key)
        if value is None or not value.strip():
            raise BusinessError(500000, f"system config missing: {key}")
        return value

    async def required_int_config(self, key: str) -> int:
        value = await self.required_config(key)
        try:
            return int(value)
        except ValueError as exc:
            raise BusinessError(500000, f"system config invalid: {key}") from exc

    async def config_or_default(self, key: str, default: str) -> str:
        value = await self._config_value(key)
        return default if value is None else value

    async def _config_value(self, key: str) -> str | None:
        return await self.db.scalar(
            select(SysConfig.config_value).where(
                SysConfig.config_key == key,
                SysConfig.deleted == 0,
                (SysConfig.builtin == 1) | (SysConfig.status == 1),
            )
        )

    async def _find(self, config_id: int) -> SysConfig:
        row = await self.db.scalar(
            select(SysConfig).where(SysConfig.id == config_id, SysConfig.deleted == 0)
        )
        if row is None:
            raise not_found()
        return row

    @staticmethod
    def _validate_value(value_type: str, value: str) -> None:
        if value_type == "boolean" and value.lower() not in {"true", "false"}:
            raise bad_request("configValue must be true or false")
        if value_type == "number":
            try:
                Decimal(value)
            except InvalidOperation as exc:
                raise bad_request("configValue must be number") from exc

    @staticmethod
    def _vo(row: SysConfig) -> dict:
        return {
            "id": str(row.id),
            "configName": row.config_name,
            "configKey": row.config_key,
            "configValue": row.config_value,
            "valueType": row.value_type,
            "builtin": row.builtin,
            "status": row.status,
            "remark": row.remark,
            "createdAt": row.created_at,
            "updatedAt": row.updated_at,
        }


def _optional(value: str | None) -> str | None:
    return value.strip() if value and value.strip() else None
