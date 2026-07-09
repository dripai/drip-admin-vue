from typing import Generic, TypeVar

from fastapi import Query
from pydantic import BaseModel, Field

from app.common.errors import bad_request

T = TypeVar("T")


class PageQuery:
    def __init__(self, page: int | None = Query(default=None), page_size: int | None = Query(default=None, alias="pageSize")):
        self.page = 1 if page is None else page
        self.page_size = 10 if page_size is None else page_size
        if self.page < 1:
            raise bad_request("page must be >= 1")
        if self.page_size < 1:
            raise bad_request("pageSize must be >= 1")
        if self.page_size > 100:
            raise bad_request("pageSize must be <= 100")


class PageResult(BaseModel, Generic[T]):
    list: list[T] = Field(default_factory=list)
    total: str
    page: int
    page_size: int = Field(alias="pageSize")

