from sqlalchemy import BigInteger,Integer,String
from sqlalchemy.orm import Mapped,mapped_column
from app.modules.system.entity.base import Base,TimestampMixin
class SysDictItem(TimestampMixin,Base):
 __tablename__="sys_dict_item"
 id:Mapped[int]=mapped_column(BigInteger,primary_key=True)
 dict_type_id:Mapped[int]=mapped_column(BigInteger)
 label:Mapped[str]=mapped_column(String(64))
 value:Mapped[str]=mapped_column(String(64))
 is_default:Mapped[int]=mapped_column(Integer)
 sort:Mapped[int]=mapped_column(Integer)
 status:Mapped[int]=mapped_column(Integer)
 builtin:Mapped[int]=mapped_column(Integer)
