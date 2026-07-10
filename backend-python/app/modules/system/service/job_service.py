import asyncio
from datetime import datetime
from sqlalchemy.ext.asyncio import AsyncSession
from app.common.id import new_id
from app.infrastructure.script_executor import ScriptExecutor
from app.modules.system.entity.sys_job import SysJob
from app.modules.system.entity.sys_job_run_log import SysJobRunLog
class JobService:
 def __init__(self,db:AsyncSession,script_dir:str):self.db=db;self.executor=ScriptExecutor(script_dir)
 async def run(self,job:SysJob):
  log=SysJobRunLog(id=new_id(),job_id=job.id,job_name=job.job_name,status="RUNNING",started_at=datetime.now(),finished_at=None,cost_ms=0,error_message=None);self.db.add(log);await self.db.commit()
  try:await asyncio.to_thread(self.executor.execute,job.executor_type,job.script_file or "",job.script_args);log.status="SUCCESS"
  except Exception as exc:log.status="FAIL";log.error_message=str(exc)[:512]
  log.finished_at=datetime.now();log.cost_ms=int((log.finished_at-log.started_at).total_seconds()*1000);await self.db.commit()
