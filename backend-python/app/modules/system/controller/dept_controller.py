from fastapi import APIRouter,Depends
from sqlalchemy.ext.asyncio import AsyncSession
from app.common.api_response import ApiResponse,success
from app.modules.system.dto.dept_request import DeptSaveRequest
from app.modules.system.dto.user_request import StatusUpdateRequest
from app.modules.system.router import get_db,require_permission
from app.modules.system.service.dept_service import DeptService
router=APIRouter(tags=["dept"])
def service(db:AsyncSession=Depends(get_db))->DeptService:return DeptService(db)
@router.get("/dept",response_model=ApiResponse)
async def tree(_:dict=Depends(require_permission("system:dept:list")),svc:DeptService=Depends(service))->ApiResponse:return success(await svc.tree())
@router.get("/dept/{dept_id}",response_model=ApiResponse)
async def detail(dept_id:int,_:dict=Depends(require_permission("system:dept:list")),svc:DeptService=Depends(service))->ApiResponse:return success(await svc.detail(dept_id))
@router.post("/dept",response_model=ApiResponse)
async def create(b:DeptSaveRequest,_:dict=Depends(require_permission("system:dept:create")),svc:DeptService=Depends(service))->ApiResponse:return success(await svc.save(None,b))
@router.put("/dept/{dept_id}",response_model=ApiResponse)
async def update(dept_id:int,b:DeptSaveRequest,_:dict=Depends(require_permission("system:dept:update")),svc:DeptService=Depends(service))->ApiResponse:await svc.save(dept_id,b);return success()
@router.delete("/dept/{dept_id}",response_model=ApiResponse)
async def delete(dept_id:int,_:dict=Depends(require_permission("system:dept:delete")),svc:DeptService=Depends(service))->ApiResponse:await svc.delete(dept_id);return success()
@router.put("/dept/{dept_id}/status",response_model=ApiResponse)
async def status(dept_id:int,b:StatusUpdateRequest,_:dict=Depends(require_permission("system:dept:update")),svc:DeptService=Depends(service))->ApiResponse:await svc.status(dept_id,b.status);return success()
