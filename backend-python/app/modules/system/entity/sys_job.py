from sqlalchemy import BigInteger, Integer, String
from sqlalchemy.orm import Mapped, mapped_column

from app.modules.system.entity.base import Base, TimestampMixin


class SysJob(TimestampMixin, Base):
    __tablename__ = "sys_job"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True)
    job_name: Mapped[str] = mapped_column(String(64))
    cron_expression: Mapped[str] = mapped_column(String(64))
    executor_type: Mapped[str] = mapped_column(String(32))
    script_file: Mapped[str | None] = mapped_column(String(255), nullable=True)
    script_args: Mapped[str | None] = mapped_column(String(1024), nullable=True)
    status: Mapped[int] = mapped_column(Integer)
    remark: Mapped[str | None] = mapped_column(String(255), nullable=True)
    deleted: Mapped[int] = mapped_column(Integer)
