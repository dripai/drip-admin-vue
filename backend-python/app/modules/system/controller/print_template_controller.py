from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.common.api_response import ApiResponse, success
from app.common.pagination import PageQuery
from app.modules.system.dto.print_template_request import PrintTemplateCopyRequest, PrintTemplateSaveRequest
from app.modules.system.dto.user_request import StatusUpdateRequest
from app.modules.system.router import get_db, require_permission
from app.modules.system.service.print_template_service import PrintTemplateService

router = APIRouter(tags=["print-template"])


def service(db: AsyncSession = Depends(get_db)) -> PrintTemplateService:
    return PrintTemplateService(db)


@router.get("/print-template", response_model=ApiResponse)
async def print_templates(
    page: PageQuery = Depends(),
    code: str | None = None,
    name: str | None = None,
    status: int | None = None,
    _: dict = Depends(require_permission("system:printTemplate:list")),
    current: PrintTemplateService = Depends(service),
) -> ApiResponse:
    return success(await current.list_templates(page, code, name, status))


@router.get("/print-template/{id}", response_model=ApiResponse)
async def print_template_detail(
    id: int,
    _: dict = Depends(require_permission("system:printTemplate:list")),
    current: PrintTemplateService = Depends(service),
) -> ApiResponse:
    return success(await current.detail(id))


@router.post("/print-template", response_model=ApiResponse)
async def create_print_template(
    body: PrintTemplateSaveRequest,
    _: dict = Depends(require_permission("system:printTemplate:create")),
    current: PrintTemplateService = Depends(service),
) -> ApiResponse:
    return success(await current.save(None, body))


@router.post("/print-template/{id}/copy", response_model=ApiResponse)
async def copy_print_template(
    id: int,
    body: PrintTemplateCopyRequest,
    _: dict = Depends(require_permission("system:printTemplate:create")),
    current: PrintTemplateService = Depends(service),
) -> ApiResponse:
    return success(await current.copy(id, body))


@router.put("/print-template/{id}", response_model=ApiResponse)
async def update_print_template(
    id: int,
    body: PrintTemplateSaveRequest,
    _: dict = Depends(require_permission("system:printTemplate:update")),
    current: PrintTemplateService = Depends(service),
) -> ApiResponse:
    await current.save(id, body)
    return success()


@router.delete("/print-template/{id}", response_model=ApiResponse)
async def delete_print_template(
    id: int,
    _: dict = Depends(require_permission("system:printTemplate:delete")),
    current: PrintTemplateService = Depends(service),
) -> ApiResponse:
    await current.delete(id)
    return success()


@router.put("/print-template/{id}/status", response_model=ApiResponse)
async def print_template_status(
    id: int,
    body: StatusUpdateRequest,
    _: dict = Depends(require_permission("system:printTemplate:update")),
    current: PrintTemplateService = Depends(service),
) -> ApiResponse:
    await current.update_status(id, body.status)
    return success()
