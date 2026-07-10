from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession
from app.common.api_response import ApiResponse, success
from app.modules.system.dto.menu_request import MenuSaveRequest
from app.modules.system.dto.user_request import StatusUpdateRequest
from app.modules.system.router import get_db, require_permission
from app.modules.system.service.menu_service import MenuService
router=APIRouter(tags=["menu"])
def service(db:AsyncSession=Depends(get_db))->MenuService:return MenuService(db)
@router.get("/menu",response_model=ApiResponse)
async def tree(_:dict=Depends(require_permission("system:menu:list")),svc:MenuService=Depends(service))->ApiResponse:return success(await svc.all_tree())
@router.post("/menu",response_model=ApiResponse)
async def create(body:MenuSaveRequest,_:dict=Depends(require_permission("system:menu:create")),svc:MenuService=Depends(service))->ApiResponse:return success(await svc.save(None,body))
@router.put("/menu/{menu_id}",response_model=ApiResponse)
async def update(menu_id:int,body:MenuSaveRequest,_:dict=Depends(require_permission("system:menu:update")),svc:MenuService=Depends(service))->ApiResponse:await svc.save(menu_id,body);return success()
@router.delete("/menu/{menu_id}",response_model=ApiResponse)
async def delete(menu_id:int,_:dict=Depends(require_permission("system:menu:delete")),svc:MenuService=Depends(service))->ApiResponse:await svc.delete(menu_id);return success()
@router.put("/menu/{menu_id}/status",response_model=ApiResponse)
async def status(menu_id:int,body:StatusUpdateRequest,_:dict=Depends(require_permission("system:menu:status")),svc:MenuService=Depends(service))->ApiResponse:await svc.update_status(menu_id,body.status);return success()
