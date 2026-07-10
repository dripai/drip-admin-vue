from collections.abc import AsyncGenerator

from fastapi import Depends, Request
from sqlalchemy.ext.asyncio import AsyncSession, async_sessionmaker

from app.common.errors import forbidden
from app.config.settings import Settings
from app.modules.system.service.auth_service import AuthService
from app.modules.system.service.permission_service import PermissionService


async def get_db(request: Request) -> AsyncGenerator[AsyncSession, None]:
    factory: async_sessionmaker[AsyncSession] = request.app.state.session_factory
    async with factory() as session:
        yield session


async def get_auth_service(request: Request, db: AsyncSession = Depends(get_db)) -> AuthService:
    return AuthService(db, request.app.state.redis, request.app.state.settings)


async def current_session(
    request: Request,
    service: AuthService = Depends(get_auth_service),
) -> dict:
    token_name = request.app.state.settings.token.name
    session = await service.current_session(request.headers.get(token_name, ""))
    request.state.session = session
    return session


def require_permission(permission: str):
    async def checker(session: dict = Depends(current_session), db: AsyncSession = Depends(get_db)) -> dict:
        permissions = await PermissionService(db).permission_codes(int(session["userId"]))
        if permission not in permissions:
            raise forbidden()
        return session

    return checker


def settings_from(request: Request) -> Settings:
    return request.app.state.settings
