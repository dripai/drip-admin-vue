from pydantic import BaseModel, Field


class MenuSaveRequest(BaseModel):
    parent_id: int | None = Field(default=None, alias="parentId")
    name: str = Field(min_length=1, max_length=64)
    type: str = Field(min_length=1, max_length=16)
    path: str | None = Field(default=None, max_length=255)
    component: str | None = Field(default=None, max_length=255)
    permission_code: str | None = Field(default=None, alias="permissionCode", max_length=128)
    icon: str | None = Field(default=None, max_length=64)
    sort: int | None = None
    visible: int | None = Field(default=None, ge=0, le=1)
    status: int | None = Field(default=None, ge=0, le=1)
