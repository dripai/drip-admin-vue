from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.common.api_response import ApiResponse, success
from app.modules.system.router import get_db
from app.modules.system.service.config_service import ConfigService
from app.common.pagination import PageQuery
from app.modules.system.dto.config_request import ConfigSaveRequest
from app.modules.system.dto.user_request import StatusUpdateRequest
from app.modules.system.router import require_permission

router = APIRouter(tags=["config"])


@router.get("/publicConfig", response_model=ApiResponse)
async def public_configs(db: AsyncSession = Depends(get_db)) -> ApiResponse:
    return success(await ConfigService(db).public_configs())

@router.get("/config",response_model=ApiResponse)
async def configs(page:PageQuery=Depends(),_:dict=Depends(require_permission("system:config:list")),db:AsyncSession=Depends(get_db))->ApiResponse:return success(await ConfigService(db).list(page))
@router.post("/config",response_model=ApiResponse)
async def create(b:ConfigSaveRequest,_:dict=Depends(require_permission("system:config:create")),db:AsyncSession=Depends(get_db))->ApiResponse:return success(await ConfigService(db).save(None,b))
@router.put("/config/{id}",response_model=ApiResponse)
async def update(id:int,b:ConfigSaveRequest,_:dict=Depends(require_permission("system:config:update")),db:AsyncSession=Depends(get_db))->ApiResponse:await ConfigService(db).save(id,b);return success()
@router.delete("/config/{id}",response_model=ApiResponse)
async def delete(id:int,_:dict=Depends(require_permission("system:config:delete")),db:AsyncSession=Depends(get_db))->ApiResponse:await ConfigService(db).delete(id);return success()
@router.put("/config/{id}/status",response_model=ApiResponse)
async def status(id:int,b:StatusUpdateRequest,_:dict=Depends(require_permission("system:config:update")),db:AsyncSession=Depends(get_db))->ApiResponse:await ConfigService(db).status(id,b.status);return success()
