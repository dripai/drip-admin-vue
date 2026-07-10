from fastapi import APIRouter,Depends
from sqlalchemy.ext.asyncio import AsyncSession
from app.common.api_response import ApiResponse,success
from app.modules.system.router import get_db,require_permission
from app.modules.system.service.dict_service import DictService
from app.modules.system.dto.dict_request import DictTypeSaveRequest,DictItemSaveRequest
router=APIRouter(tags=["dict"])
@router.get("/dict/type",response_model=ApiResponse)
async def types(_:dict=Depends(require_permission("system:dict:list")),db:AsyncSession=Depends(get_db))->ApiResponse:return success(await DictService(db).types())
@router.get("/dict/type/{id}/item",response_model=ApiResponse)
async def items(id:int,_:dict=Depends(require_permission("system:dict:list")),db:AsyncSession=Depends(get_db))->ApiResponse:return success(await DictService(db).items(id))
@router.post("/dict/type",response_model=ApiResponse)
async def create_type(b:DictTypeSaveRequest,_:dict=Depends(require_permission("system:dict:create")),db:AsyncSession=Depends(get_db))->ApiResponse:return success(await DictService(db).create_type(b))
@router.post("/dict/item",response_model=ApiResponse)
async def create_item(b:DictItemSaveRequest,_:dict=Depends(require_permission("system:dict:create")),db:AsyncSession=Depends(get_db))->ApiResponse:return success(await DictService(db).create_item(b))
@router.post("/dict/cache/refresh",response_model=ApiResponse)
async def refresh(_:dict=Depends(require_permission("system:dict:update")),db:AsyncSession=Depends(get_db))->ApiResponse:await DictService(db).refresh_cache();return success()
@router.delete("/dict/type/{id}",response_model=ApiResponse)
async def delete_type(id:int,_:dict=Depends(require_permission("system:dict:delete")),db:AsyncSession=Depends(get_db))->ApiResponse:await DictService(db).delete_type(id);return success()
@router.delete("/dict/item/{id}",response_model=ApiResponse)
async def delete_item(id:int,_:dict=Depends(require_permission("system:dict:delete")),db:AsyncSession=Depends(get_db))->ApiResponse:await DictService(db).delete_item(id);return success()
