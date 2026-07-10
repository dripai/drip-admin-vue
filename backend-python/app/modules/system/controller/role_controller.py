from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.common.api_response import ApiResponse, success
from app.common.pagination import PageQuery
from app.modules.system.dto.role_request import MenuAssignRequest, RoleSaveRequest
from app.modules.system.dto.user_request import StatusUpdateRequest
from app.modules.system.router import get_db, require_permission
from app.modules.system.service.role_service import RoleService

router = APIRouter(tags=["role"])
def service(db: AsyncSession = Depends(get_db)) -> RoleService: return RoleService(db)

@router.get("/role", response_model=ApiResponse)
async def roles(page: PageQuery = Depends(), role_name: str|None = Query(None, alias="roleName"), role_code: str|None = Query(None, alias="roleCode"), status: int|None = None, _:dict=Depends(require_permission("system:role:list")), svc:RoleService=Depends(service))->ApiResponse: return success(await svc.list(page,role_name,role_code,status))
@router.get("/role/option", response_model=ApiResponse)
async def options(_:dict=Depends(require_permission("system:role:list")), svc:RoleService=Depends(service))->ApiResponse: return success(await svc.options())
@router.get("/role/{role_id}", response_model=ApiResponse)
async def role(role_id:int,_:dict=Depends(require_permission("system:role:list")),svc:RoleService=Depends(service))->ApiResponse:return success(await svc.detail(role_id))
@router.get("/role/{role_id}/user", response_model=ApiResponse)
async def users(role_id:int,page:PageQuery=Depends(),_:dict=Depends(require_permission("system:role:list")),svc:RoleService=Depends(service))->ApiResponse:return success(await svc.users(role_id,page))
@router.get("/role/{role_id}/permission", response_model=ApiResponse)
async def permissions(role_id:int,_:dict=Depends(require_permission("system:role:permission")),svc:RoleService=Depends(service))->ApiResponse:return success(await svc.permissions(role_id))
@router.post("/role", response_model=ApiResponse)
async def create(body:RoleSaveRequest,_:dict=Depends(require_permission("system:role:create")),svc:RoleService=Depends(service))->ApiResponse:return success(await svc.create(body))
@router.put("/role/{role_id}", response_model=ApiResponse)
async def update(role_id:int,body:RoleSaveRequest,_:dict=Depends(require_permission("system:role:update")),svc:RoleService=Depends(service))->ApiResponse: await svc.update(role_id,body); return success()
@router.delete("/role/{role_id}", response_model=ApiResponse)
async def delete(role_id:int,_:dict=Depends(require_permission("system:role:delete")),svc:RoleService=Depends(service))->ApiResponse: await svc.delete(role_id); return success()
@router.put("/role/{role_id}/status", response_model=ApiResponse)
async def status(role_id:int,body:StatusUpdateRequest,_:dict=Depends(require_permission("system:role:update")),svc:RoleService=Depends(service))->ApiResponse: await svc.update_status(role_id,body.status); return success()
@router.put("/role/{role_id}/permission", response_model=ApiResponse)
async def assign(role_id:int,body:MenuAssignRequest,_:dict=Depends(require_permission("system:role:permission")),svc:RoleService=Depends(service))->ApiResponse: await svc.assign_menus(role_id,body); return success()
