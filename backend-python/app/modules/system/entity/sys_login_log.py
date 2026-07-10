from datetime import datetime

from sqlalchemy import BigInteger, DateTime, String
from sqlalchemy.orm import Mapped, mapped_column

from app.modules.system.entity.base import Base


class SysLoginLog(Base):
    __tablename__ = "sys_login_log"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True)
    user_id: Mapped[int | None] = mapped_column(BigInteger, nullable=True)
    username: Mapped[str] = mapped_column(String(64))
    real_name: Mapped[str | None] = mapped_column(String(64), nullable=True)
    login_type: Mapped[str] = mapped_column(String(32))
    status: Mapped[str] = mapped_column(String(16))
    failure_reason: Mapped[str | None] = mapped_column(String(255), nullable=True)
    ip: Mapped[str | None] = mapped_column(String(64), nullable=True)
    user_agent: Mapped[str | None] = mapped_column(String(512), nullable=True)
    device_type: Mapped[str | None] = mapped_column(String(64), nullable=True)
    login_at: Mapped[datetime] = mapped_column(DateTime)
