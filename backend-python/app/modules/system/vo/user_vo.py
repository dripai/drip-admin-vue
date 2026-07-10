from datetime import datetime

from pydantic import BaseModel, Field


class RoleSummaryVo(BaseModel):
    id: str
    role_name: str = Field(alias="roleName")
    role_code: str = Field(alias="roleCode")


class DeptSummaryVo(BaseModel):
    id: str
    dept_name: str = Field(alias="deptName")


class UserListVo(BaseModel):
    id: str
    username: str
    real_name: str = Field(alias="realName")
    phone: str | None
    email: str | None
    status: int
    dept: DeptSummaryVo | None
    roles: list[RoleSummaryVo]
    created_at: datetime | None = Field(alias="createdAt")
    last_login_at: datetime | None = Field(alias="lastLoginAt")


class UserDetailVo(UserListVo):
    dept_id: str | None = Field(alias="deptId")
    remark: str | None
