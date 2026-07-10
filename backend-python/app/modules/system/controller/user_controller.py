from fastapi import APIRouter, Depends, Query, Request
from sqlalchemy.ext.asyncio import AsyncSession
from redis.asyncio import Redis

from app.common.api_response import ApiResponse, success
from app.common.pagination import PageQuery
from app.modules.system.dto.user_request import PasswordResetRequest, RoleAssignRequest, StatusUpdateRequest, UserSaveRequest
from app.modules.system.router import current_session, get_db, require_permission
from app.modules.system.service.user_service import UserService

router = APIRouter(tags=["user"])


def get_user_service(request: Request, db: AsyncSession = Depends(get_db)) -> UserService:
    return UserService(db, request.app.state.redis)


@router.get("/user", response_model=ApiResponse)
async def list_users(
    page: PageQuery = Depends(),
    username: str | None = None,
    real_name: str | None = Query(default=None, alias="realName"),
    phone: str | None = None,
    status: int | None = None,
    dept_id: int | None = Query(default=None, alias="deptId"),
    role_id: int | None = Query(default=None, alias="roleId"),
    _: dict = Depends(require_permission("system:user:list")),
    service: UserService = Depends(get_user_service),
) -> ApiResponse:
    return success(await service.list_users(page, username, real_name, phone, status, dept_id, role_id))


@router.get("/user/{user_id}", response_model=ApiResponse)
async def user_detail(
    user_id: int, _: dict = Depends(require_permission("system:user:detail")), service: UserService = Depends(get_user_service)
) -> ApiResponse:
    return success(await service.detail(user_id))


@router.post("/user", response_model=ApiResponse)
async def create_user(
    body: UserSaveRequest, _: dict = Depends(require_permission("system:user:create")), service: UserService = Depends(get_user_service)
) -> ApiResponse:
    return success(await service.create(body))


@router.put("/user/{user_id}", response_model=ApiResponse)
async def update_user(
    user_id: int, body: UserSaveRequest, _: dict = Depends(require_permission("system:user:update")),
    service: UserService = Depends(get_user_service),
) -> ApiResponse:
    await service.update(user_id, body)
    return success()


@router.delete("/user/{user_id}", response_model=ApiResponse)
async def delete_user(
    user_id: int, session: dict = Depends(require_permission("system:user:delete")), service: UserService = Depends(get_user_service)
) -> ApiResponse:
    await service.delete(int(session["userId"]), user_id)
    return success()


@router.put("/user/{user_id}/status", response_model=ApiResponse)
async def user_status(
    user_id: int, body: StatusUpdateRequest, session: dict = Depends(require_permission("system:user:disable")),
    service: UserService = Depends(get_user_service),
) -> ApiResponse:
    await service.update_status(int(session["userId"]), user_id, body)
    return success()


@router.post("/user/{user_id}/unlock", response_model=ApiResponse)
async def unlock_user(
    user_id: int, _: dict = Depends(require_permission("system:user:unlock")), service: UserService = Depends(get_user_service)
) -> ApiResponse:
    await service.unlock(user_id)
    return success()


@router.put("/user/{user_id}/role", response_model=ApiResponse)
async def assign_user_roles(
    user_id: int, body: RoleAssignRequest, session: dict = Depends(require_permission("system:user:assignRole")),
    service: UserService = Depends(get_user_service),
) -> ApiResponse:
    await service.assign_roles(int(session["userId"]), user_id, body)
    return success()


@router.post("/user/{user_id}/resetPassword", response_model=ApiResponse)
async def reset_password(
    user_id: int, body: PasswordResetRequest, session: dict = Depends(require_permission("system:user:resetPassword")),
    service: UserService = Depends(get_user_service),
) -> ApiResponse:
    await service.reset_password(int(session["userId"]), user_id, body)
    return success()
