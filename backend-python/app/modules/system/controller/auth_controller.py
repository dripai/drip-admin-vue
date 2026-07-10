from fastapi import APIRouter, Depends, Request

from app.common.api_response import ApiResponse, success
from app.modules.system.dto.auth_request import LoginRequest, PasswordRequest, ProfileUpdateRequest
from app.modules.system.router import current_session, get_auth_service
from app.modules.system.service.auth_service import AuthService

router = APIRouter(tags=["auth"])


@router.post("/login", response_model=ApiResponse)
async def login(request: Request, body: LoginRequest, service: AuthService = Depends(get_auth_service)) -> ApiResponse:
    return success(await service.login(body, request.client.host if request.client else "", request.headers.get("User-Agent", "")))


@router.post("/logout", response_model=ApiResponse)
async def logout(session: dict = Depends(current_session), service: AuthService = Depends(get_auth_service)) -> ApiResponse:
    await service.logout(session)
    return success()


@router.get("/me", response_model=ApiResponse)
async def me(session: dict = Depends(current_session), service: AuthService = Depends(get_auth_service)) -> ApiResponse:
    return success(await service.me(session))


@router.put("/password", response_model=ApiResponse)
async def change_password(
    body: PasswordRequest, session: dict = Depends(current_session), service: AuthService = Depends(get_auth_service)
) -> ApiResponse:
    await service.change_password(session, body)
    return success()


@router.put("/profile", response_model=ApiResponse)
async def update_profile(
    body: ProfileUpdateRequest, session: dict = Depends(current_session), service: AuthService = Depends(get_auth_service)
) -> ApiResponse:
    await service.update_profile(session, body)
    return success()
