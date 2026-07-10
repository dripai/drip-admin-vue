import json
import logging
import time
from datetime import datetime
from typing import Any

from fastapi import Request
from starlette.middleware.base import BaseHTTPMiddleware

from app.common.id import new_id
from app.modules.system.entity import SysOperationLog

logger = logging.getLogger("drip.operation")

_DEFINITIONS = {
    "PUT /api/system/profile": ("personal profile", "update profile"),
    "POST /api/system/user": ("user", "create"),
    "PUT /api/system/user/{id}": ("user", "update"),
    "DELETE /api/system/user/{id}": ("user", "delete"),
    "PUT /api/system/user/{id}/status": ("user", "update status"),
    "POST /api/system/user/{id}/unlock": ("user", "unlock"),
    "PUT /api/system/user/{id}/role": ("user", "assign role"),
    "POST /api/system/user/{id}/resetPassword": ("user", "reset password"),
    "POST /api/system/role": ("role", "create"),
    "PUT /api/system/role/{id}": ("role", "update"),
    "DELETE /api/system/role/{id}": ("role", "delete"),
    "PUT /api/system/role/{id}/status": ("role", "update status"),
    "PUT /api/system/role/{id}/permission": ("role", "assign permission"),
    "POST /api/system/menu": ("menu", "create"),
    "PUT /api/system/menu/{id}": ("menu", "update"),
    "DELETE /api/system/menu/{id}": ("menu", "delete"),
    "PUT /api/system/menu/{id}/status": ("menu", "update status"),
    "POST /api/system/dept": ("department", "create"),
    "PUT /api/system/dept/{id}": ("department", "update"),
    "DELETE /api/system/dept/{id}": ("department", "delete"),
    "PUT /api/system/dept/{id}/status": ("department", "update status"),
    "POST /api/system/config": ("config", "create"),
    "PUT /api/system/config/{id}": ("config", "update"),
    "DELETE /api/system/config/{id}": ("config", "delete"),
    "PUT /api/system/config/{id}/status": ("config", "update status"),
    "POST /api/system/dict/type": ("dictionary", "create type"),
    "PUT /api/system/dict/type/{id}": ("dictionary", "update type"),
    "DELETE /api/system/dict/type/{id}": ("dictionary", "delete type"),
    "POST /api/system/dict/item": ("dictionary", "create item"),
    "PUT /api/system/dict/item/{id}": ("dictionary", "update item"),
    "DELETE /api/system/dict/item/{id}": ("dictionary", "delete item"),
    "PUT /api/system/dict/item/{id}/status": ("dictionary", "update item status"),
    "POST /api/system/onlineUser/{tokenId}/kickout": ("online user", "kickout"),
    "POST /api/system/job": ("job", "create"),
    "PUT /api/system/job/{id}": ("job", "update"),
    "DELETE /api/system/job/{id}": ("job", "delete"),
    "PUT /api/system/job/{id}/status": ("job", "update status"),
    "POST /api/system/job/{id}/run": ("job", "run"),
    "POST /api/system/files": ("file", "upload"),
    "POST /api/system/print-template": ("print template", "create"),
    "POST /api/system/print-template/{id}/copy": ("print template", "copy"),
    "PUT /api/system/print-template/{id}": ("print template", "update"),
    "DELETE /api/system/print-template/{id}": ("print template", "delete"),
    "PUT /api/system/print-template/{id}/status": ("print template", "update status"),
}


class OperationLoggingMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next):
        started = time.perf_counter()
        params = await _request_params(request)
        response = await call_next(request)
        route = request.scope.get("route")
        template = getattr(route, "path", request.url.path)
        definition = _DEFINITIONS.get(f"{request.method} {template}")
        if definition is not None:
            await self._write(request, response.status_code, definition, params, started)
        return response

    @staticmethod
    async def _write(
        request: Request,
        status_code: int,
        definition: tuple[str, str],
        params: str | None,
        started: float,
    ) -> None:
        session = getattr(request.state, "session", None)
        operator_id = int(session["userId"]) if session else None
        operator_name = (session.get("realName") or session.get("username")) if session else None
        factory = request.app.state.session_factory
        row = SysOperationLog(
            id=new_id(),
            operator_id=operator_id,
            operator_name=operator_name,
            module=definition[0],
            action=definition[1],
            method=request.method,
            path=request.url.path,
            request_params=params,
            response_status="SUCCESS" if status_code < 400 else "FAIL",
            error_message=None if status_code < 400 else f"HTTP {status_code}",
            cost_ms=int((time.perf_counter() - started) * 1000),
            created_at=datetime.now(),
        )
        try:
            async with factory() as db:
                db.add(row)
                await db.commit()
        except Exception:
            logger.exception("operation log write failed method=%s path=%s", request.method, request.url.path)


async def _request_params(request: Request) -> str | None:
    params: dict[str, Any] = {}
    if request.query_params:
        params["query"] = _mask(dict(request.query_params))
    content_type = request.headers.get("content-type", "")
    if content_type.startswith("application/json"):
        body = await request.body()
        if body:
            try:
                params["body"] = _mask(json.loads(body))
            except (json.JSONDecodeError, UnicodeDecodeError):
                params["body"] = "invalid json"
    elif content_type.startswith("multipart/"):
        params["contentType"] = content_type.split(";", 1)[0]
    if not params:
        return None
    return json.dumps(params, ensure_ascii=False, separators=(",", ":"))[:8192]


def _mask(value: Any) -> Any:
    if isinstance(value, dict):
        return {
            key: "******" if _sensitive(key) else _mask(item)
            for key, item in value.items()
        }
    if isinstance(value, list):
        return [_mask(item) for item in value]
    return value


def _sensitive(key: str) -> bool:
    normalized = key.lower()
    return any(item in normalized for item in ("password", "token", "secret", "authorization"))
