from pydantic import BaseModel, Field


class AuthLoginVo(BaseModel):
    token: str
    expire_at: str = Field(alias="expireAt")
    active_timeout_seconds: int = Field(alias="activeTimeoutSeconds")
    token_timeout_seconds: int = Field(alias="tokenTimeoutSeconds")
    device_type: str = Field(alias="deviceType")


class AuthMeVo(BaseModel):
    id: str
    username: str
    real_name: str = Field(alias="realName")
    phone: str | None
    email: str | None
    avatar: str | None
    dept_id: str | None = Field(alias="deptId")
    roles: list[str]
    permissions: list[str]
    menus: list[dict]

