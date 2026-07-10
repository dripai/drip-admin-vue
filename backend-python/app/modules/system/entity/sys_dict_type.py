from sqlalchemy import BigInteger,Integer,String
from sqlalchemy.orm import Mapped,mapped_column
from app.modules.system.entity.base import Base,TimestampMixin
class SysDictType(TimestampMixin,Base):
 __tablename__="sys_dict_type"
 id:Mapped[int]=mapped_column(BigInteger,primary_key=True)
 dict_name:Mapped[str]=mapped_column(String(64))
 dict_code:Mapped[str]=mapped_column(String(64))
 status:Mapped[int]=mapped_column(Integer)
 builtin:Mapped[int]=mapped_column(Integer)
 remark:Mapped[str|None]=mapped_column(String(255),nullable=True)
