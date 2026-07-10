from sqlalchemy import func,select
from sqlalchemy.ext.asyncio import AsyncSession
from app.common.errors import BusinessError,bad_request,not_found
from app.common.id import new_id
from app.modules.system.dto.dept_request import DeptSaveRequest
from app.modules.system.entity import SysDept,SysUser
class DeptService:
 def __init__(self,db:AsyncSession):self.db=db
 async def tree(self)->list[dict]:
  rows=(await self.db.scalars(select(SysDept).where(SysDept.deleted==0).order_by(SysDept.sort,SysDept.id))).all(); nodes={r.id:self._vo(r) for r in rows}; roots=[]
  for r in rows:(nodes[r.parent_id]["children"] if r.parent_id in nodes else roots).append(nodes[r.id])
  return roots
 async def detail(self,id:int)->dict:return self._vo(await self._find(id))
 async def save(self,id:int|None,b:DeptSaveRequest)->str|None:
  if not b.dept_name.strip() or not b.dept_code.strip():raise bad_request("deptName and deptCode are required")
  if b.status is not None and b.status not in {0,1}:raise bad_request("status is invalid")
  parent=b.parent_id or 0
  if parent and not await self.db.scalar(select(SysDept.id).where(SysDept.id==parent,SysDept.deleted==0)):raise not_found()
  dup=select(SysDept.id).where(SysDept.dept_code==b.dept_code.strip(),SysDept.deleted==0)
  if id:dup=dup.where(SysDept.id!=id)
  if await self.db.scalar(dup):raise BusinessError(409000,"deptCode already exists")
  if b.leader_user_id and not await self.db.scalar(select(SysUser.id).where(SysUser.id==b.leader_user_id,SysUser.deleted==0)):raise bad_request("leaderUserId is invalid")
  values=dict(parent_id=parent,dept_name=b.dept_name.strip(),dept_code=b.dept_code.strip(),leader_user_id=b.leader_user_id,sort=b.sort or 0,status=1 if b.status is None else b.status)
  if id is None:
   row=SysDept(id=new_id(),deleted=0,**values);self.db.add(row);await self.db.commit();return str(row.id)
  row=await self._find(id)
  if parent==id:raise bad_request("operation failed")
  for k,v in values.items():setattr(row,k,v)
  await self.db.commit();return None
 async def delete(self,id:int)->None:
  row=await self._find(id)
  if await self.db.scalar(select(func.count()).select_from(SysDept).where(SysDept.parent_id==id,SysDept.deleted==0)):raise bad_request("operation failed")
  if await self.db.scalar(select(func.count()).select_from(SysUser).where(SysUser.dept_id==id,SysUser.deleted==0)):raise BusinessError(409000,"operation failed")
  row.deleted=1;await self.db.commit()
 async def status(self,id:int,status:int)->None:
  if status not in {0,1}:raise bad_request("status is invalid")
  row=await self._find(id);row.status=status;await self.db.commit()
 async def _find(self,id:int)->SysDept:
  row=await self.db.scalar(select(SysDept).where(SysDept.id==id,SysDept.deleted==0))
  if not row:raise not_found()
  return row
 @staticmethod
 def _vo(r:SysDept)->dict:return {"id":str(r.id),"parentId":str(r.parent_id),"deptName":r.dept_name,"deptCode":r.dept_code,"leaderUserId":str(r.leader_user_id) if r.leader_user_id else None,"sort":r.sort,"status":r.status,"children":[]}
