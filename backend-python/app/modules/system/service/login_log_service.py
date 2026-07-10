from datetime import datetime
import logging

from sqlalchemy.ext.asyncio import AsyncSession

from app.common.id import new_id
from app.modules.system.entity import SysLoginLog

logger = logging.getLogger("drip.login")


class LoginLogService:
    def __init__(self, db: AsyncSession) -> None:
        self.db = db

    async def write(
        self,
        user_id: int | None,
        username: str,
        real_name: str | None,
        login_type: str,
        status: str,
        failure_reason: str | None,
        ip: str,
        user_agent: str,
        device_type: str,
    ) -> None:
        try:
            self.db.add(
                SysLoginLog(
                id=new_id(),
                user_id=user_id,
                username=username,
                real_name=real_name,
                login_type=login_type,
                status=status,
                failure_reason=failure_reason,
                ip=ip,
                user_agent=user_agent,
                device_type=device_type,
                login_at=datetime.now(),
                )
            )
            await self.db.commit()
        except Exception:
            await self.db.rollback()
            logger.exception("login log write failed username=%s status=%s", username, status)
