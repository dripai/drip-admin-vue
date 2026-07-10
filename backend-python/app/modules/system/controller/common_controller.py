from datetime import UTC, datetime

from fastapi import APIRouter
from fastapi.responses import RedirectResponse, Response

from app.common.api_response import ApiResponse, success

router = APIRouter(tags=["common"])


@router.get("/", include_in_schema=False)
async def root() -> RedirectResponse:
    return RedirectResponse(url="swagger-ui/index.html", status_code=302)


@router.get("/favicon.ico", include_in_schema=False)
async def favicon() -> Response:
    return Response(status_code=204)


@router.get("/health", response_model=ApiResponse)
async def health() -> ApiResponse:
    return success(
        {
            "status": "UP",
            "service": "drip-admin-backend",
            "timestamp": datetime.now(UTC).isoformat().replace("+00:00", "Z"),
        }
    )
