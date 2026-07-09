from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.modules.system.entity import SysConfig


class ConfigService:
    def __init__(self, db: AsyncSession) -> None:
        self.db = db

    async def public_configs(self) -> dict[str, str]:
        query = select(SysConfig.config_key, SysConfig.config_value).where(
            SysConfig.config_type == "public", SysConfig.status == 1, SysConfig.deleted == 0
        )
        return dict((await self.db.execute(query)).all())

