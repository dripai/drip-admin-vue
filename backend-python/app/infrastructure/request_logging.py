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
            logger.exception("http request failed method=%s path=%s ip=%s", request.method, request.url.path, _client_ip(request))
            raise
        logger.info(
            "http request completed method=%s path=%s status=%s costMs=%d ip=%s",
            request.method,
            request.url.path,
            response.status_code,
            (time.perf_counter() - started) * 1000,
            _client_ip(request),
        )
        return response


def _client_ip(request: Request) -> str:
    return request.client.host if request.client else ""
