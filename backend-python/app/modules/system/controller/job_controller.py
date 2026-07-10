from fastapi import APIRouter, Depends, Query, Request
from sqlalchemy.ext.asyncio import AsyncSession

from app.common.api_response import ApiResponse, success
from app.common.pagination import PageQuery
from app.modules.system.dto.job_request import JobSaveRequest
from app.modules.system.dto.user_request import StatusUpdateRequest
from app.modules.system.router import get_db, require_permission
from app.modules.system.service.job_service import JobService

router = APIRouter(tags=["job"])


def service(request: Request, db: AsyncSession = Depends(get_db)) -> JobService:
    return JobService(db, request.app.state.settings.job.script_dir)


@router.get("/job", response_model=ApiResponse)
async def jobs(
    page: PageQuery = Depends(),
    job_name: str | None = Query(default=None, alias="jobName"),
    remark: str | None = None,
    status: int | None = None,
    created_at: str | None = Query(default=None, alias="createdAt"),
    _: dict = Depends(require_permission("system:job:list")),
    current: JobService = Depends(service),
) -> ApiResponse:
    return success(await current.list_jobs(page, job_name, remark, status, created_at))


@router.get("/job/scripts", response_model=ApiResponse)
async def job_scripts(
    executor_type: str = Query(alias="executorType"),
    _: dict = Depends(require_permission("system:job:list")),
    current: JobService = Depends(service),
) -> ApiResponse:
    return success(current.executor.list_scripts(executor_type))


@router.get("/job/{id}", response_model=ApiResponse)
async def job_detail(
    id: int,
    _: dict = Depends(require_permission("system:job:list")),
    current: JobService = Depends(service),
) -> ApiResponse:
    return success(await current.detail(id))


@router.post("/job", response_model=ApiResponse)
async def create_job(
    body: JobSaveRequest,
    _: dict = Depends(require_permission("system:job:create")),
    current: JobService = Depends(service),
) -> ApiResponse:
    return success(await current.save(None, body))


@router.put("/job/{id}", response_model=ApiResponse)
async def update_job(
    id: int,
    body: JobSaveRequest,
    _: dict = Depends(require_permission("system:job:update")),
    current: JobService = Depends(service),
) -> ApiResponse:
    await current.save(id, body)
    return success()


@router.delete("/job/{id}", response_model=ApiResponse)
async def delete_job(
    id: int,
    _: dict = Depends(require_permission("system:job:delete")),
    current: JobService = Depends(service),
) -> ApiResponse:
    await current.delete(id)
    return success()


@router.put("/job/{id}/status", response_model=ApiResponse)
async def job_status(
    id: int,
    body: StatusUpdateRequest,
    _: dict = Depends(require_permission("system:job:update")),
    current: JobService = Depends(service),
) -> ApiResponse:
    await current.update_status(id, body.status)
    return success()


@router.post("/job/{id}/run", response_model=ApiResponse)
async def run_job(
    id: int,
    _: dict = Depends(require_permission("system:job:run")),
    current: JobService = Depends(service),
) -> ApiResponse:
    await current.run(id)
    return success()


@router.get("/job/{id}/runLog", response_model=ApiResponse)
async def job_run_logs(
    id: int,
    page: PageQuery = Depends(),
    job_name: str | None = Query(default=None, alias="jobName"),
    status: str | None = None,
    started_range: list[str] | None = Query(default=None, alias="startedRange"),
    _: dict = Depends(require_permission("system:job:list")),
    current: JobService = Depends(service),
) -> ApiResponse:
    return success(await current.run_logs(page, id, job_name, status, started_range))


@router.get("/jobRunLog", response_model=ApiResponse)
async def job_run_log_list(
    page: PageQuery = Depends(),
    job_name: str | None = Query(default=None, alias="jobName"),
    status: str | None = None,
    started_range: list[str] | None = Query(default=None, alias="startedRange"),
    _: dict = Depends(require_permission("system:job:history")),
    current: JobService = Depends(service),
) -> ApiResponse:
    return success(await current.run_logs(page, None, job_name, status, started_range))
