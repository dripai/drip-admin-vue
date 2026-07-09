from sqlalchemy import BigInteger
from sqlalchemy.orm import Mapped, mapped_column

from app.modules.system.entity.base import Base


class SysUserRole(Base):
    __tablename__ = "sys_user_role"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True)
    user_id: Mapped[int] = mapped_column(BigInteger)
    role_id: Mapped[int] = mapped_column(BigInteger)

