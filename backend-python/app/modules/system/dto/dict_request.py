from pydantic import BaseModel, Field


class DictTypeSaveRequest(BaseModel):
    dict_name: str = Field(alias="dictName", min_length=1, max_length=64)
    dict_code: str = Field(alias="dictCode", min_length=1, max_length=64)
    status: int | None = Field(default=None, ge=0, le=1)
    builtin: int | None = Field(default=None, ge=0, le=1)
    remark: str | None = Field(default=None, max_length=255)


class DictItemSaveRequest(BaseModel):
    dict_type_id: int = Field(alias="dictTypeId")
    label: str = Field(min_length=1, max_length=64)
    value: str = Field(min_length=1, max_length=64)
    is_default: int | None = Field(default=None, alias="isDefault", ge=0, le=1)
    sort: int | None = None
    status: int | None = Field(default=None, ge=0, le=1)
    builtin: int | None = Field(default=None, ge=0, le=1)
