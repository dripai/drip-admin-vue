from fastapi import APIRouter, Depends, File, Request, UploadFile
from sqlalchemy.ext.asyncio import AsyncSession

from app.common.api_response import ApiResponse, success
from app.modules.system.router import get_db, require_permission
from app.modules.system.service.file_service import FileService

router = APIRouter(tags=["file"])


@router.post("/files", response_model=ApiResponse)
async def upload_file(
    request: Request,
    file: UploadFile = File(...),
    _: dict = Depends(require_permission("system:file:upload")),
    db: AsyncSession = Depends(get_db),
) -> ApiResponse:
    service = FileService(db, request.app.state.settings.file.upload_dir)
    return success(await service.upload(file))
