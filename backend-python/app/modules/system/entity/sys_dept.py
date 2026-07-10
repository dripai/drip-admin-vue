from sqlalchemy import BigInteger, Integer, String
from sqlalchemy.orm import Mapped, mapped_column

from app.modules.system.entity.base import Base, TimestampMixin


class SysDept(TimestampMixin, Base):
    __tablename__ = "sys_dept"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True)
    parent_id: Mapped[int] = mapped_column(BigInteger)
    dept_name: Mapped[str] = mapped_column(String(64))
    dept_code: Mapped[str] = mapped_column(String(64))
    leader_user_id: Mapped[int | None] = mapped_column(BigInteger, nullable=True)
    sort: Mapped[int] = mapped_column(Integer)
    status: Mapped[int] = mapped_column(Integer)
    deleted: Mapped[int] = mapped_column(Integer)
