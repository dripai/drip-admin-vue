from datetime import datetime

from fastapi import APIRouter, Depends, Query, Request
from sqlalchemy.ext.asyncio import AsyncSession

from app.common.api_response import ApiResponse, success
from app.common.pagination import PageQuery
from app.modules.system.dto.user_request import PasswordResetRequest, RoleAssignRequest, StatusUpdateRequest, UserSaveRequest
from app.modules.system.router import get_db, require_permission
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
    created_from: datetime | None = Query(default=None, alias="createdFrom"),
    created_to: datetime | None = Query(default=None, alias="createdTo"),
    _: dict = Depends(require_permission("system:user:list")),
    service: UserService = Depends(get_user_service),
) -> ApiResponse:
    return success(
        await service.list_users(
            page,
            username,
            real_name,
            phone,
            status,
            dept_id,
            role_id,
            created_from,
            created_to,
        )
    )


@router.get("/user/{id}", response_model=ApiResponse)
async def user_detail(
    id: int, _: dict = Depends(require_permission("system:user:detail")), service: UserService = Depends(get_user_service)
) -> ApiResponse:
    return success(await service.detail(id))


@router.post("/user", response_model=ApiResponse)
async def create_user(
    body: UserSaveRequest, _: dict = Depends(require_permission("system:user:create")), service: UserService = Depends(get_user_service)
) -> ApiResponse:
    return success(await service.create(body))


@router.put("/user/{id}", response_model=ApiResponse)
async def update_user(
    id: int, body: UserSaveRequest, _: dict = Depends(require_permission("system:user:update")),
    service: UserService = Depends(get_user_service),
) -> ApiResponse:
    await service.update(id, body)
    return success()


@router.delete("/user/{id}", response_model=ApiResponse)
async def delete_user(
    id: int, session: dict = Depends(require_permission("system:user:delete")), service: UserService = Depends(get_user_service)
) -> ApiResponse:
    await service.delete(int(session["userId"]), id)
    return success()


@router.put("/user/{id}/status", response_model=ApiResponse)
async def user_status(
    id: int, body: StatusUpdateRequest, session: dict = Depends(require_permission("system:user:disable")),
    service: UserService = Depends(get_user_service),
) -> ApiResponse:
    await service.update_status(int(session["userId"]), id, body)
    return success()


@router.post("/user/{id}/unlock", response_model=ApiResponse)
async def unlock_user(
    id: int, _: dict = Depends(require_permission("system:user:unlock")), service: UserService = Depends(get_user_service)
) -> ApiResponse:
    await service.unlock(id)
    return success()


@router.put("/user/{id}/role", response_model=ApiResponse)
async def assign_user_roles(
    id: int, body: RoleAssignRequest, session: dict = Depends(require_permission("system:user:assignRole")),
    service: UserService = Depends(get_user_service),
) -> ApiResponse:
    await service.assign_roles(int(session["userId"]), id, body)
    return success()


@router.post("/user/{id}/resetPassword", response_model=ApiResponse)
async def reset_password(
    id: int, body: PasswordResetRequest, session: dict = Depends(require_permission("system:user:resetPassword")),
    service: UserService = Depends(get_user_service),
) -> ApiResponse:
    await service.reset_password(int(session["userId"]), id, body)
    return success()
