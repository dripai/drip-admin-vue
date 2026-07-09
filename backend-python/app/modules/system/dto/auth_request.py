from pydantic import BaseModel, Field


class LoginRequest(BaseModel):
    username: str
    password: str
    device_type: str = Field(alias="deviceType")


class PasswordRequest(BaseModel):
    old_password: str = Field(alias="oldPassword")
    new_password: str = Field(alias="newPassword")


class ProfileUpdateRequest(BaseModel):
    real_name: str = Field(alias="realName")
    phone: str | None = None
    email: str | None = None

