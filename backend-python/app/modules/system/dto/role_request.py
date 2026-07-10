from pydantic import BaseModel, Field


class RoleSaveRequest(BaseModel):
    role_name: str = Field(alias="roleName", min_length=1, max_length=64)
    role_code: str = Field(alias="roleCode", min_length=1, max_length=64)
    status: int | None = Field(default=None, ge=0, le=1)
    remark: str | None = Field(default=None, max_length=255)


class MenuAssignRequest(BaseModel):
    menu_ids: list[int] = Field(alias="menuIds")
