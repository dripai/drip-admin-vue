from pydantic import BaseModel,Field
class DictTypeSaveRequest(BaseModel):
 dict_name:str=Field(alias="dictName");dict_code:str=Field(alias="dictCode");status:int|None=None;remark:str|None=None
class DictItemSaveRequest(BaseModel):
 dict_type_id:int=Field(alias="dictTypeId");label:str;value:str;is_default:int|None=Field(None,alias="isDefault");sort:int|None=None;status:int|None=None
