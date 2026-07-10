from pydantic import BaseModel, Field
class DeptSaveRequest(BaseModel):
    parent_id:int|None=Field(None,alias="parentId")
    dept_name:str=Field(alias="deptName")
    dept_code:str=Field(alias="deptCode")
    leader_user_id:int|None=Field(None,alias="leaderUserId")
    sort:int|None=None
    status:int|None=None
