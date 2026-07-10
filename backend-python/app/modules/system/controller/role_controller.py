from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.common.api_response import ApiResponse, success
from app.common.pagination import PageQuery
from app.modules.system.dto.role_request import MenuAssignRequest, RoleSaveRequest
from app.modules.system.dto.user_request import StatusUpdateRequest
from app.modules.system.router import get_db, require_permission
from app.modules.system.service.role_service import RoleService

router = APIRouter(tags=["role"])


def service(db: AsyncSession = Depends(get_db)) -> RoleService:
    return RoleService(db)


@router.get("/role", response_model=ApiResponse)
async def roles(
    page: PageQuery = Depends(),
    role_name: str | None = Query(default=None, alias="roleName"),
    role_code: str | None = Query(default=None, alias="roleCode"),
    status: int | None = None,
    created_at: str | None = Query(default=None, alias="createdAt"),
    _: dict = Depends(require_permission("system:role:list")),
    current: RoleService = Depends(service),
) -> ApiResponse:
    return success(await current.list_roles(page, role_name, role_code, status, created_at))


@router.get("/role/option", response_model=ApiResponse)
async def role_options(
    _: dict = Depends(require_permission("system:role:list")),
    current: RoleService = Depends(service),
) -> ApiResponse:
    return success(await current.options())


@router.get("/role/{id}", response_model=ApiResponse)
async def role_detail(
    id: int,
    _: dict = Depends(require_permission("system:role:list")),
    current: RoleService = Depends(service),
) -> ApiResponse:
    return success(await current.detail(id))


@router.get("/role/{id}/user", response_model=ApiResponse)
async def role_users(
    id: int,
    page: PageQuery = Depends(),
    _: dict = Depends(require_permission("system:role:list")),
    current: RoleService = Depends(service),
) -> ApiResponse:
    return success(await current.users(id, page))


@router.get("/role/{id}/permission", response_model=ApiResponse)
async def role_permissions(
    id: int,
    _: dict = Depends(require_permission("system:role:permission")),
    current: RoleService = Depends(service),
) -> ApiResponse:
    return success(await current.permissions(id))


@router.post("/role", response_model=ApiResponse)
async def create_role(
    body: RoleSaveRequest,
    _: dict = Depends(require_permission("system:role:create")),
    current: RoleService = Depends(service),
) -> ApiResponse:
    return success(await current.create(body))


@router.put("/role/{id}", response_model=ApiResponse)
async def update_role(
    id: int,
    body: RoleSaveRequest,
    _: dict = Depends(require_permission("system:role:update")),
    current: RoleService = Depends(service),
) -> ApiResponse:
    await current.update(id, body)
    return success()


@router.delete("/role/{id}", response_model=ApiResponse)
async def delete_role(
    id: int,
    _: dict = Depends(require_permission("system:role:delete")),
    current: RoleService = Depends(service),
) -> ApiResponse:
    await current.delete(id)
    return success()


@router.put("/role/{id}/status", response_model=ApiResponse)
async def role_status(
    id: int,
    body: StatusUpdateRequest,
    _: dict = Depends(require_permission("system:role:update")),
    current: RoleService = Depends(service),
) -> ApiResponse:
    await current.update_status(id, body.status)
    return success()


@router.put("/role/{id}/permission", response_model=ApiResponse)
async def assign_role_permissions(
    id: int,
    body: MenuAssignRequest,
    _: dict = Depends(require_permission("system:role:permission")),
    current: RoleService = Depends(service),
) -> ApiResponse:
    await current.assign_menus(id, body)
    return success()
