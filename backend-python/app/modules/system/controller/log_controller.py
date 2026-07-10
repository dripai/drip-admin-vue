from datetime import datetime

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.common.api_response import ApiResponse, success
from app.common.pagination import PageQuery
from app.modules.system.router import get_db, require_permission
from app.modules.system.service.log_service import LogService

router = APIRouter(tags=["system-log"])


def service(db: AsyncSession = Depends(get_db)) -> LogService:
    return LogService(db)


@router.get("/loginLog", response_model=ApiResponse)
async def login_logs(
    page: PageQuery = Depends(),
    username: str | None = None,
    status: str | None = None,
    login_type: str | None = Query(default=None, alias="loginType"),
    device_type: str | None = Query(default=None, alias="deviceType"),
    ip: str | None = None,
    login_from: datetime | None = Query(default=None, alias="loginFrom"),
    login_to: datetime | None = Query(default=None, alias="loginTo"),
    _: dict = Depends(require_permission("system:loginLog:list")),
    current: LogService = Depends(service),
) -> ApiResponse:
    return success(
        await current.login_logs(
            page,
            username,
            status,
            login_type,
            device_type,
            ip,
            login_from,
            login_to,
        )
    )


@router.get("/loginLog/{id}", response_model=ApiResponse)
async def login_log_detail(
    id: int,
    _: dict = Depends(require_permission("system:loginLog:list")),
    current: LogService = Depends(service),
) -> ApiResponse:
    return success(await current.login_log(id))


@router.get("/operationLog", response_model=ApiResponse)
async def operation_logs(
    page: PageQuery = Depends(),
    operator: str | None = None,
    module: str | None = None,
    action: str | None = None,
    status: str | None = None,
    path: str | None = None,
    created_from: datetime | None = Query(default=None, alias="createdFrom"),
    created_to: datetime | None = Query(default=None, alias="createdTo"),
    _: dict = Depends(require_permission("system:operationLog:list")),
    current: LogService = Depends(service),
) -> ApiResponse:
    return success(
        await current.operation_logs(
            page,
            operator,
            module,
            action,
            status,
            path,
            created_from,
            created_to,
        )
    )


@router.get("/operationLog/{id}", response_model=ApiResponse)
async def operation_log_detail(
    id: int,
    _: dict = Depends(require_permission("system:operationLog:list")),
    current: LogService = Depends(service),
) -> ApiResponse:
    return success(await current.operation_log(id))
