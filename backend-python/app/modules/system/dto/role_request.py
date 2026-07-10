from pydantic import BaseModel, Field


class RoleSaveRequest(BaseModel):
    role_name: str = Field(alias="roleName")
    role_code: str = Field(alias="roleCode")
    status: int | None = None
    remark: str | None = None


class MenuAssignRequest(BaseModel):
    menu_ids: list[int] = Field(alias="menuIds")
