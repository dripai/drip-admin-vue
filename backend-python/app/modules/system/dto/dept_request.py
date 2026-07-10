from pydantic import BaseModel, Field


class DeptSaveRequest(BaseModel):
    parent_id: int | None = Field(default=None, alias="parentId")
    dept_name: str = Field(alias="deptName", min_length=1, max_length=64)
    dept_code: str = Field(alias="deptCode", min_length=1, max_length=64)
    leader_user_id: int | None = Field(default=None, alias="leaderUserId")
    sort: int | None = None
    status: int | None = Field(default=None, ge=0, le=1)
