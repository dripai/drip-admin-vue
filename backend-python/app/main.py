from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.exceptions import RequestValidationError

from app.common.errors import (
    BusinessError,
    business_error_handler,
    unexpected_error_handler,
    validation_error_handler,
)
from app.config.settings import Settings, load_settings
from app.infrastructure.database import create_session_factory
from app.infrastructure.redis_client import create_redis_client
from app.infrastructure.request_logging import RequestLoggingMiddleware
from app.modules.system.controller import auth_controller, common_controller, config_controller, dept_controller, dict_controller, job_controller, menu_controller, role_controller, user_controller


def create_app(settings: Settings | None = None) -> FastAPI:
    resolved_settings = settings or load_settings()

    @asynccontextmanager
    async def lifespan(app: FastAPI):
        engine, session_factory = create_session_factory(resolved_settings)
        app.state.settings = resolved_settings
        app.state.engine = engine
        app.state.session_factory = session_factory
        app.state.redis = create_redis_client(resolved_settings)
        try:
            yield
        finally:
            await app.state.redis.aclose()
            await engine.dispose()

    app = FastAPI(
        title="Drip Admin API",
        version="0.1.0",
        lifespan=lifespan,
        docs_url="/api/swagger-ui.html",
        redoc_url=None,
        openapi_url="/api/v3/api-docs",
    )
    app.add_exception_handler(BusinessError, business_error_handler)
    app.add_exception_handler(RequestValidationError, validation_error_handler)
    app.add_exception_handler(Exception, unexpected_error_handler)
    app.add_middleware(RequestLoggingMiddleware)
    app.include_router(common_controller.router, prefix="/api")
    app.include_router(auth_controller.router, prefix="/api/system")
    app.include_router(config_controller.router, prefix="/api/system")
    app.include_router(user_controller.router, prefix="/api/system")
    app.include_router(role_controller.router, prefix="/api/system")
    app.include_router(menu_controller.router, prefix="/api/system")
    app.include_router(dept_controller.router, prefix="/api/system")
    app.include_router(dict_controller.router, prefix="/api/system")
    app.include_router(job_controller.router, prefix="/api/system")
    return app


app = create_app()
