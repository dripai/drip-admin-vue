from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from app.common.errors import bad_request,not_found
from app.common.id import new_id
from app.modules.system.entity import SysDictItem,SysDictType
from app.common.id import new_id
from app.common.errors import bad_request,not_found
from app.modules.system.dto.dict_request import DictTypeSaveRequest,DictItemSaveRequest
class DictService:
 def __init__(self,db:AsyncSession):self.db=db
 async def types(self):return [self.type_vo(x) for x in (await self.db.scalars(select(SysDictType))).all()]
 async def items(self,id:int):return [self.item_vo(x) for x in (await self.db.scalars(select(SysDictItem).where(SysDictItem.dict_type_id==id))).all()]
 async def create_type(self,b:DictTypeSaveRequest):
  if not b.dict_name.strip() or not b.dict_code.strip():raise bad_request("dictName and dictCode are required")
  x=SysDictType(id=new_id(),dict_name=b.dict_name,dict_code=b.dict_code,status=b.status or 1,builtin=0,remark=b.remark);self.db.add(x);await self.db.commit();return str(x.id)
 async def create_item(self,b:DictItemSaveRequest):
  if not b.label.strip() or not b.value.strip():raise bad_request("label and value are required")
  if not await self.db.scalar(select(SysDictType.id).where(SysDictType.id==b.dict_type_id)):raise not_found()
  x=SysDictItem(id=new_id(),dict_type_id=b.dict_type_id,label=b.label,value=b.value,is_default=b.is_default or 0,sort=b.sort or 0,status=b.status or 1,builtin=0);self.db.add(x);await self.db.commit();return str(x.id)
 async def refresh_cache(self):
  return None
 async def delete_type(self,id:int):
  x=await self.db.scalar(select(SysDictType).where(SysDictType.id==id))
  if not x:raise not_found()
  if x.builtin:raise bad_request("operation failed")
  await self.db.delete(x);await self.db.commit()
 async def delete_item(self,id:int):
  x=await self.db.scalar(select(SysDictItem).where(SysDictItem.id==id))
  if not x:raise not_found()
  if x.builtin:raise bad_request("operation failed")
  await self.db.delete(x);await self.db.commit()
 @staticmethod
 def type_vo(x):return {"id":str(x.id),"dictName":x.dict_name,"dictCode":x.dict_code,"status":x.status,"builtin":x.builtin,"remark":x.remark}
 @staticmethod
 def item_vo(x):return {"id":str(x.id),"dictTypeId":str(x.dict_type_id),"label":x.label,"value":x.value,"isDefault":x.is_default,"sort":x.sort,"status":x.status,"builtin":x.builtin}
