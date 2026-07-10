from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.common.api_response import ApiResponse, success
from app.common.pagination import PageQuery
from app.modules.system.dto.config_request import ConfigSaveRequest
from app.modules.system.dto.user_request import StatusUpdateRequest
from app.modules.system.router import get_db, require_permission
from app.modules.system.service.config_service import ConfigService

router = APIRouter(tags=["config"])


@router.get("/publicConfig", response_model=ApiResponse)
async def public_configs(db: AsyncSession = Depends(get_db)) -> ApiResponse:
    return success(await ConfigService(db).public_configs())


@router.get("/config", response_model=ApiResponse)
async def configs(
    page: PageQuery = Depends(),
    config_name: str | None = Query(default=None, alias="configName"),
    config_key: str | None = Query(default=None, alias="configKey"),
    status: int | None = None,
    _: dict = Depends(require_permission("system:config:list")),
    db: AsyncSession = Depends(get_db),
) -> ApiResponse:
    return success(await ConfigService(db).list_configs(page, config_name, config_key, status))


@router.post("/config", response_model=ApiResponse)
async def create_config(
    body: ConfigSaveRequest,
    _: dict = Depends(require_permission("system:config:create")),
    db: AsyncSession = Depends(get_db),
) -> ApiResponse:
    return success(await ConfigService(db).save(None, body))


@router.put("/config/{id}", response_model=ApiResponse)
async def update_config(
    id: int,
    body: ConfigSaveRequest,
    _: dict = Depends(require_permission("system:config:update")),
    db: AsyncSession = Depends(get_db),
) -> ApiResponse:
    await ConfigService(db).save(id, body)
    return success()


@router.delete("/config/{id}", response_model=ApiResponse)
async def delete_config(
    id: int,
    _: dict = Depends(require_permission("system:config:delete")),
    db: AsyncSession = Depends(get_db),
) -> ApiResponse:
    await ConfigService(db).delete(id)
    return success()


@router.put("/config/{id}/status", response_model=ApiResponse)
async def config_status(
    id: int,
    body: StatusUpdateRequest,
    _: dict = Depends(require_permission("system:config:update")),
    db: AsyncSession = Depends(get_db),
) -> ApiResponse:
    await ConfigService(db).update_status(id, body.status)
    return success()
