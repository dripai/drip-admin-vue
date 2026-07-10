from datetime import datetime

from sqlalchemy import BigInteger, DateTime, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.modules.system.entity.base import Base


class SysOperationLog(Base):
    __tablename__ = "sys_operation_log"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True)
    operator_id: Mapped[int | None] = mapped_column(BigInteger, nullable=True)
    operator_name: Mapped[str | None] = mapped_column(String(64), nullable=True)
    module: Mapped[str] = mapped_column(String(64))
    action: Mapped[str] = mapped_column(String(64))
    method: Mapped[str] = mapped_column(String(16))
    path: Mapped[str] = mapped_column(String(255))
    request_params: Mapped[str | None] = mapped_column(Text, nullable=True)
    response_status: Mapped[str] = mapped_column(String(16))
    error_message: Mapped[str | None] = mapped_column(Text, nullable=True)
    cost_ms: Mapped[int] = mapped_column(BigInteger)
    created_at: Mapped[datetime] = mapped_column(DateTime)
