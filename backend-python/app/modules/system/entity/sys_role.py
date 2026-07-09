from sqlalchemy import BigInteger, Integer, String
from sqlalchemy.orm import Mapped, mapped_column

from app.modules.system.entity.base import Base, TimestampMixin


class SysRole(TimestampMixin, Base):
    __tablename__ = "sys_role"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True)
    role_name: Mapped[str] = mapped_column(String(64))
    role_code: Mapped[str] = mapped_column(String(64))
    builtin: Mapped[int] = mapped_column(Integer)
    status: Mapped[int] = mapped_column(Integer)
    remark: Mapped[str | None] = mapped_column(String(500), nullable=True)
    deleted: Mapped[int] = mapped_column(Integer)

