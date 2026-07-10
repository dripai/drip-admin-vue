from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.common.api_response import ApiResponse, success
from app.modules.system.dto.dept_request import DeptSaveRequest
from app.modules.system.dto.user_request import StatusUpdateRequest
from app.modules.system.router import get_db, require_permission
from app.modules.system.service.dept_service import DeptService

router = APIRouter(tags=["department"])


def service(db: AsyncSession = Depends(get_db)) -> DeptService:
    return DeptService(db)


@router.get("/dept", response_model=ApiResponse)
async def departments(
    _: dict = Depends(require_permission("system:dept:list")),
    current: DeptService = Depends(service),
) -> ApiResponse:
    return success(await current.tree())


@router.get("/dept/{id}", response_model=ApiResponse)
async def department_detail(
    id: int,
    _: dict = Depends(require_permission("system:dept:list")),
    current: DeptService = Depends(service),
) -> ApiResponse:
    return success(await current.detail(id))


@router.post("/dept", response_model=ApiResponse)
async def create_department(
    body: DeptSaveRequest,
    _: dict = Depends(require_permission("system:dept:create")),
    current: DeptService = Depends(service),
) -> ApiResponse:
    return success(await current.save(None, body))


@router.put("/dept/{id}", response_model=ApiResponse)
async def update_department(
    id: int,
    body: DeptSaveRequest,
    _: dict = Depends(require_permission("system:dept:update")),
    current: DeptService = Depends(service),
) -> ApiResponse:
    await current.save(id, body)
    return success()


@router.delete("/dept/{id}", response_model=ApiResponse)
async def delete_department(
    id: int,
    _: dict = Depends(require_permission("system:dept:delete")),
    current: DeptService = Depends(service),
) -> ApiResponse:
    await current.delete(id)
    return success()


@router.put("/dept/{id}/status", response_model=ApiResponse)
async def department_status(
    id: int,
    body: StatusUpdateRequest,
    _: dict = Depends(require_permission("system:dept:update")),
    current: DeptService = Depends(service),
) -> ApiResponse:
    await current.update_status(id, body.status)
    return success()
