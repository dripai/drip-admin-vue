from pydantic import BaseModel, Field


class UserSaveRequest(BaseModel):
    username: str
    real_name: str = Field(alias="realName")
    phone: str | None = None
    email: str | None = None
    status: int | None = None
    dept_id: int | None = Field(default=None, alias="deptId")
    remark: str | None = None
    password: str | None = None

class StatusUpdateRequest(BaseModel):
    status: int


class RoleAssignRequest(BaseModel):
    role_ids: list[int] = Field(alias="roleIds")


class PasswordResetRequest(BaseModel):
    password: str | None = None
