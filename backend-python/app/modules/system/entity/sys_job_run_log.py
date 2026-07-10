from datetime import datetime

from sqlalchemy import BigInteger, DateTime, String
from sqlalchemy.orm import Mapped, mapped_column

from app.modules.system.entity.base import Base


class SysJobRunLog(Base):
    __tablename__ = "sys_job_run_log"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True)
    job_id: Mapped[int] = mapped_column(BigInteger)
    job_name: Mapped[str] = mapped_column(String(64))
    status: Mapped[str] = mapped_column(String(16))
    started_at: Mapped[datetime] = mapped_column(DateTime)
    finished_at: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)
    cost_ms: Mapped[int] = mapped_column(BigInteger)
    error_message: Mapped[str | None] = mapped_column(String(512), nullable=True)
