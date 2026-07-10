from pydantic import BaseModel,Field
class ConfigSaveRequest(BaseModel):
 config_name:str=Field(alias="configName")
 config_key:str=Field(alias="configKey")
 config_value:str=Field(alias="configValue")
 config_type:str=Field(alias="configType")
 status:int|None=None
 remark:str|None=None
