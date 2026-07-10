from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.common.api_response import ApiResponse, success
from app.modules.system.dto.menu_request import MenuSaveRequest
from app.modules.system.dto.user_request import StatusUpdateRequest
from app.modules.system.router import get_db, require_permission
from app.modules.system.service.menu_service import MenuService

router = APIRouter(tags=["menu"])


def service(db: AsyncSession = Depends(get_db)) -> MenuService:
    return MenuService(db)


@router.get("/menu", response_model=ApiResponse)
async def menu_tree(
    _: dict = Depends(require_permission("system:menu:list")),
    current: MenuService = Depends(service),
) -> ApiResponse:
    return success(await current.all_tree())


@router.post("/menu", response_model=ApiResponse)
async def create_menu(
    body: MenuSaveRequest,
    _: dict = Depends(require_permission("system:menu:create")),
    current: MenuService = Depends(service),
) -> ApiResponse:
    return success(await current.save(None, body))


@router.put("/menu/{id}", response_model=ApiResponse)
async def update_menu(
    id: int,
    body: MenuSaveRequest,
    _: dict = Depends(require_permission("system:menu:update")),
    current: MenuService = Depends(service),
) -> ApiResponse:
    await current.save(id, body)
    return success()


@router.delete("/menu/{id}", response_model=ApiResponse)
async def delete_menu(
    id: int,
    _: dict = Depends(require_permission("system:menu:delete")),
    current: MenuService = Depends(service),
) -> ApiResponse:
    await current.delete(id)
    return success()


@router.put("/menu/{id}/status", response_model=ApiResponse)
async def menu_status(
    id: int,
    body: StatusUpdateRequest,
    _: dict = Depends(require_permission("system:menu:status")),
    current: MenuService = Depends(service),
) -> ApiResponse:
    await current.update_status(id, body.status)
    return success()
