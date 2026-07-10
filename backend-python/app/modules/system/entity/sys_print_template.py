from sqlalchemy import BigInteger, Integer, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.modules.system.entity.base import Base, TimestampMixin


class SysPrintTemplate(TimestampMixin, Base):
    __tablename__ = "sys_print_template"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True)
    code: Mapped[str] = mapped_column(String(64))
    name: Mapped[str] = mapped_column(String(100))
    paper_type: Mapped[str] = mapped_column(String(32))
    template_json: Mapped[str] = mapped_column(Text)
    status: Mapped[int] = mapped_column(Integer)
    deleted: Mapped[int] = mapped_column(Integer)
