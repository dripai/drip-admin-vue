from pydantic import BaseModel, Field


class PrintTemplateSaveRequest(BaseModel):
    code: str = Field(min_length=1, max_length=64)
    name: str = Field(min_length=1, max_length=100)
    paper_type: str = Field(alias="paperType", min_length=1, max_length=32)
    template_json: str = Field(alias="templateJson", min_length=1)
    status: int = Field(ge=0, le=1)


class PrintTemplateCopyRequest(BaseModel):
    code: str = Field(min_length=1, max_length=64)
    name: str = Field(min_length=1, max_length=100)
    status: int = Field(ge=0, le=1)
