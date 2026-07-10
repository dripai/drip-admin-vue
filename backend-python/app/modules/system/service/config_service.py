from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.modules.system.entity import SysConfig
from app.common.errors import BusinessError,bad_request,not_found
from app.common.id import new_id
from app.common.pagination import PageQuery,PageResult
from app.modules.system.dto.config_request import ConfigSaveRequest


class ConfigService:
    def __init__(self, db: AsyncSession) -> None:
        self.db = db

    async def public_configs(self) -> dict[str, str]:
        query = select(SysConfig.config_key, SysConfig.config_value).where(
            SysConfig.config_type == "public", SysConfig.status == 1, SysConfig.deleted == 0
        )
        return dict((await self.db.execute(query)).all())
    async def list(self,page:PageQuery)->PageResult[dict]:
        where=[SysConfig.deleted==0]; total=await self.db.scalar(select(func.count()).select_from(SysConfig).where(*where)) or 0
        rows=(await self.db.scalars(select(SysConfig).where(*where).offset((page.page-1)*page.page_size).limit(page.page_size))).all()
        return PageResult(list=[self._vo(r) for r in rows],total=str(total),page=page.page,pageSize=page.page_size)
    async def save(self,id:int|None,b:ConfigSaveRequest)->str|None:
        if not b.config_name.strip() or not b.config_key.strip():raise bad_request("configName and configKey are required")
        if b.status is not None and b.status not in {0,1}:raise bad_request("status is invalid")
        q=select(SysConfig.id).where(SysConfig.config_key==b.config_key.strip(),SysConfig.deleted==0)
        if id:q=q.where(SysConfig.id!=id)
        if await self.db.scalar(q):raise BusinessError(409000,"configKey already exists")
        vals=dict(config_name=b.config_name.strip(),config_key=b.config_key.strip(),config_value=b.config_value,config_type=b.config_type,status=1 if b.status is None else b.status,remark=b.remark)
        if id is None:
            row=SysConfig(id=new_id(),deleted=0,**vals);self.db.add(row);await self.db.commit();return str(row.id)
        row=await self.db.scalar(select(SysConfig).where(SysConfig.id==id,SysConfig.deleted==0))
        if not row:raise not_found()
        for k,v in vals.items():setattr(row,k,v)
        await self.db.commit();return None
    async def delete(self,id:int)->None:
        row=await self.db.scalar(select(SysConfig).where(SysConfig.id==id,SysConfig.deleted==0))
        if not row:raise not_found()
        row.deleted=1;await self.db.commit()
    async def status(self,id:int,status:int)->None:
        if status not in {0,1}:raise bad_request("status is invalid")
        row=await self.db.scalar(select(SysConfig).where(SysConfig.id==id,SysConfig.deleted==0))
        if not row:raise not_found()
        row.status=status;await self.db.commit()
    @staticmethod
    def _vo(r:SysConfig)->dict:return {"id":str(r.id),"configName":r.config_name,"configKey":r.config_key,"configValue":r.config_value,"configType":r.config_type,"status":r.status,"remark":r.remark}
