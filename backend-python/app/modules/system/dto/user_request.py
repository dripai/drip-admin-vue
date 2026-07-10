from pydantic import BaseModel, Field


class UserSaveRequest(BaseModel):
    username: str = Field(min_length=1, max_length=64)
    real_name: str = Field(alias="realName", min_length=1, max_length=64)
    phone: str | None = Field(default=None, max_length=32)
    email: str | None = Field(default=None, max_length=128)
    status: int | None = Field(default=None, ge=0, le=1)
    dept_id: int | None = Field(default=None, alias="deptId")
    remark: str | None = Field(default=None, max_length=255)
    password: str | None = Field(default=None, max_length=64)

class StatusUpdateRequest(BaseModel):
    status: int | None = Field(default=None, ge=0, le=1)


class RoleAssignRequest(BaseModel):
    role_ids: list[int] = Field(alias="roleIds")


class PasswordResetRequest(BaseModel):
    password: str | None = Field(default=None, max_length=64)
