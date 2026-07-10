from fastapi import APIRouter, Depends, Request
from sqlalchemy.ext.asyncio import AsyncSession

from app.common.api_response import ApiResponse, success
from app.modules.system.dto.dict_request import DictItemSaveRequest, DictTypeSaveRequest
from app.modules.system.dto.user_request import StatusUpdateRequest
from app.modules.system.router import get_db, require_permission
from app.modules.system.service.dict_service import DictService

router = APIRouter(tags=["dict"])


def service(request: Request, db: AsyncSession = Depends(get_db)) -> DictService:
    return DictService(db, request.app.state.redis)


@router.get("/dict/type", response_model=ApiResponse)
async def dict_types(
    _: dict = Depends(require_permission("system:dict:list")),
    current: DictService = Depends(service),
) -> ApiResponse:
    return success(await current.types())


@router.post("/dict/type", response_model=ApiResponse)
async def create_dict_type(
    body: DictTypeSaveRequest,
    _: dict = Depends(require_permission("system:dict:create")),
    current: DictService = Depends(service),
) -> ApiResponse:
    return success(await current.save_type(None, body))


@router.put("/dict/type/{id}", response_model=ApiResponse)
async def update_dict_type(
    id: int,
    body: DictTypeSaveRequest,
    _: dict = Depends(require_permission("system:dict:update")),
    current: DictService = Depends(service),
) -> ApiResponse:
    await current.save_type(id, body)
    return success()


@router.delete("/dict/type/{id}", response_model=ApiResponse)
async def delete_dict_type(
    id: int,
    _: dict = Depends(require_permission("system:dict:delete")),
    current: DictService = Depends(service),
) -> ApiResponse:
    await current.delete_type(id)
    return success()


@router.get("/dict/type/{id}/item", response_model=ApiResponse)
async def dict_items(
    id: int,
    _: dict = Depends(require_permission("system:dict:list")),
    current: DictService = Depends(service),
) -> ApiResponse:
    return success(await current.items(id))


@router.post("/dict/item", response_model=ApiResponse)
async def create_dict_item(
    body: DictItemSaveRequest,
    _: dict = Depends(require_permission("system:dict:create")),
    current: DictService = Depends(service),
) -> ApiResponse:
    return success(await current.save_item(None, body))


@router.put("/dict/item/{id}", response_model=ApiResponse)
async def update_dict_item(
    id: int,
    body: DictItemSaveRequest,
    _: dict = Depends(require_permission("system:dict:update")),
    current: DictService = Depends(service),
) -> ApiResponse:
    await current.save_item(id, body)
    return success()


@router.delete("/dict/item/{id}", response_model=ApiResponse)
async def delete_dict_item(
    id: int,
    _: dict = Depends(require_permission("system:dict:delete")),
    current: DictService = Depends(service),
) -> ApiResponse:
    await current.delete_item(id)
    return success()


@router.put("/dict/item/{id}/status", response_model=ApiResponse)
async def dict_item_status(
    id: int,
    body: StatusUpdateRequest,
    _: dict = Depends(require_permission("system:dict:update")),
    current: DictService = Depends(service),
) -> ApiResponse:
    await current.update_item_status(id, body.status)
    return success()


@router.post("/dict/cache/refresh", response_model=ApiResponse)
async def refresh_dict_cache(
    _: dict = Depends(require_permission("system:dict:update")),
    current: DictService = Depends(service),
) -> ApiResponse:
    await current.refresh_cache()
    return success()
