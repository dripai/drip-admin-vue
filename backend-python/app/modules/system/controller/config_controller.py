from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.common.api_response import ApiResponse, success
from app.modules.system.router import get_db
from app.modules.system.service.config_service import ConfigService

router = APIRouter(tags=["config"])


@router.get("/publicConfig", response_model=ApiResponse)
async def public_configs(db: AsyncSession = Depends(get_db)) -> ApiResponse:
    return success(await ConfigService(db).public_configs())

