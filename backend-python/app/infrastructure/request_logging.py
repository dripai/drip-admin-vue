import logging
import time

from fastapi import Request
from starlette.middleware.base import BaseHTTPMiddleware

logger = logging.getLogger("drip.request")


class RequestLoggingMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next):
        started = time.perf_counter()
        try:
            response = await call_next(request)
        except Exception:
            logger.exception(
                "http request failed",
                extra={
                    "method": request.method,
                    "path": request.url.path,
                    "ip": _client_ip(request),
                    "userAgent": request.headers.get("user-agent", ""),
                },
            )
            raise
        logger.info(
            "http request completed",
            extra={
                "method": request.method,
                "path": request.url.path,
                "status": response.status_code,
                "costMs": int((time.perf_counter() - started) * 1000),
                "ip": _client_ip(request),
                "userAgent": request.headers.get("user-agent", ""),
            },
        )
        return response


def _client_ip(request: Request) -> str:
    return request.client.host if request.client else ""
