from fastapi import APIRouter, Depends, Query, Request

from app.common.api_response import ApiResponse, success
from app.common.pagination import PageQuery
from app.modules.system.router import require_permission
from app.modules.system.service.online_user_service import OnlineUserService

router = APIRouter(tags=["online-user"])


def service(request: Request) -> OnlineUserService:
    return OnlineUserService(request.app.state.redis)


@router.get("/onlineUser", response_model=ApiResponse)
async def online_users(
    request: Request,
    page: PageQuery = Depends(),
    username: str | None = None,
    ip: str | None = None,
    device_type: str | None = Query(default=None, alias="deviceType"),
    _: dict = Depends(require_permission("system:online:list")),
    current: OnlineUserService = Depends(service),
) -> ApiResponse:
    token = request.headers.get(request.app.state.settings.token.name, "")
    return success(await current.list_users(page, token, username, ip, device_type))


@router.get("/onlineUser/{tokenId}", response_model=ApiResponse)
async def online_user_detail(
    tokenId: str,
    request: Request,
    _: dict = Depends(require_permission("system:online:list")),
    current: OnlineUserService = Depends(service),
) -> ApiResponse:
    token = request.headers.get(request.app.state.settings.token.name, "")
    return success(await current.detail(tokenId, token))


@router.post("/onlineUser/{tokenId}/kickout", response_model=ApiResponse)
async def kickout_online_user(
    tokenId: str,
    request: Request,
    _: dict = Depends(require_permission("system:online:kickout")),
    current: OnlineUserService = Depends(service),
) -> ApiResponse:
    token = request.headers.get(request.app.state.settings.token.name, "")
    await current.kickout(tokenId, token)
    return success()
