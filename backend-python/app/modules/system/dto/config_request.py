from pydantic import BaseModel, Field


class ConfigSaveRequest(BaseModel):
    config_name: str = Field(alias="configName", min_length=1, max_length=128)
    config_key: str = Field(alias="configKey", min_length=1, max_length=128)
    config_value: str = Field(alias="configValue", max_length=1024)
    value_type: str = Field(alias="valueType", pattern="^(string|boolean|number)$")
    status: int | None = Field(default=None, ge=0, le=1)
    remark: str | None = Field(default=None, max_length=255)
