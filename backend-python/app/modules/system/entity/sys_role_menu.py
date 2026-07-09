from sqlalchemy import BigInteger
from sqlalchemy.orm import Mapped, mapped_column

from app.modules.system.entity.base import Base


class SysRoleMenu(Base):
    __tablename__ = "sys_role_menu"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True)
    role_id: Mapped[int] = mapped_column(BigInteger)
    menu_id: Mapped[int] = mapped_column(BigInteger)

