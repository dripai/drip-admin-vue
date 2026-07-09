from typing import Any

from pydantic import BaseModel


class ApiResponse(BaseModel):
    code: int
    message: str
    data: Any = None


def success(data: Any = None) -> ApiResponse:
    return ApiResponse(code=0, message="success", data=data)


def failure(code: int, message: str) -> ApiResponse:
    return ApiResponse(code=code, message=message, data=None)

