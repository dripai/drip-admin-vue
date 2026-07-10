from pydantic import BaseModel, Field


class MenuSaveRequest(BaseModel):
    parent_id: int | None = Field(default=None, alias="parentId")
    name: str
    type: str
    path: str | None = None
    component: str | None = None
    permission_code: str | None = Field(default=None, alias="permissionCode")
    icon: str | None = None
    sort: int | None = None
    visible: int | None = None
    status: int | None = None
