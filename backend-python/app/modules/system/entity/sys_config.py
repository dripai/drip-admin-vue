from sqlalchemy import BigInteger, Integer, String
from sqlalchemy.orm import Mapped, mapped_column

from app.modules.system.entity.base import Base, TimestampMixin


class SysConfig(TimestampMixin, Base):
    __tablename__ = "sys_config"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True)
    config_name: Mapped[str] = mapped_column(String(128))
    config_key: Mapped[str] = mapped_column(String(128))
    config_value: Mapped[str] = mapped_column(String(2000))
    config_type: Mapped[str] = mapped_column(String(32))
    status: Mapped[int] = mapped_column(Integer)
    remark: Mapped[str | None] = mapped_column(String(500), nullable=True)
    deleted: Mapped[int] = mapped_column(Integer)

