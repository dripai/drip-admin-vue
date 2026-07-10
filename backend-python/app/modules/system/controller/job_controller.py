from fastapi import APIRouter,Depends,Request
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from app.common.api_response import ApiResponse,success
from app.modules.system.entity.sys_job import SysJob
from app.modules.system.entity.sys_job_run_log import SysJobRunLog
from app.modules.system.router import get_db,require_permission
from app.modules.system.service.job_service import JobService
from app.common.errors import not_found
router=APIRouter(tags=["job"])
@router.get("/job/scripts",response_model=ApiResponse)
async def scripts(executorType:str,request:Request,_:dict=Depends(require_permission("system:job:list")),db:AsyncSession=Depends(get_db))->ApiResponse:return success(JobService(db,request.app.state.settings.job.script_dir).executor.list_scripts(executorType))
@router.post("/job/{id}/run",response_model=ApiResponse)
async def run(id:int,request:Request,_:dict=Depends(require_permission("system:job:run")),db:AsyncSession=Depends(get_db))->ApiResponse:
 job=await db.scalar(select(SysJob).where(SysJob.id==id,SysJob.deleted==0))
 if not job:raise not_found()
 await JobService(db,request.app.state.settings.job.script_dir).run(job);return success()
@router.get("/job",response_model=ApiResponse)
async def jobs(_:dict=Depends(require_permission("system:job:list")),db:AsyncSession=Depends(get_db))->ApiResponse:return success([{"id":str(x.id),"jobName":x.job_name,"executorType":x.executor_type,"scriptFile":x.script_file,"status":x.status} for x in (await db.scalars(select(SysJob).where(SysJob.deleted==0))).all()])
@router.get("/jobRunLog",response_model=ApiResponse)
async def logs(_:dict=Depends(require_permission("system:job:history")),db:AsyncSession=Depends(get_db))->ApiResponse:return success([{"id":str(x.id),"jobId":str(x.job_id),"status":x.status,"startedAt":x.started_at,"finishedAt":x.finished_at,"costMs":str(x.cost_ms),"errorMessage":x.error_message} for x in (await db.scalars(select(SysJobRunLog))).all()])
