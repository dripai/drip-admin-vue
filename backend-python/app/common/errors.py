from fastapi import Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse

from app.common.api_response import failure


class BusinessError(Exception):
    def __init__(self, code: int, message: str) -> None:
        self.code = code
        self.message = message
        super().__init__(message)

    @property
    def status_code(self) -> int:
        return {
            401000: 401,
            403000: 403,
            404000: 404,
            409000: 409,
        }.get(self.code, 500 if self.code >= 500000 else 400)


def bad_request(message: str) -> BusinessError:
    return BusinessError(400000, message)


def unauthorized(message: str = "\u672a\u767b\u5f55\u6216 token \u5931\u6548") -> BusinessError:
    return BusinessError(401000, message)


def forbidden(message: str = "\u65e0\u6743\u9650") -> BusinessError:
    return BusinessError(403000, message)


def not_found(message: str = "operation failed") -> BusinessError:
    return BusinessError(404000, message)


async def business_error_handler(_: Request, exc: BusinessError) -> JSONResponse:
    return JSONResponse(status_code=exc.status_code, content=failure(exc.code, exc.message).model_dump())


async def validation_error_handler(_: Request, __: RequestValidationError) -> JSONResponse:
    return JSONResponse(status_code=400, content=failure(400000, "\u8bf7\u6c42\u4f53 JSON \u683c\u5f0f\u9519\u8bef").model_dump())


async def unexpected_error_handler(_: Request, __: Exception) -> JSONResponse:
    return JSONResponse(status_code=500, content=failure(500000, "\u7cfb\u7edf\u5185\u90e8\u9519\u8bef").model_dump())

