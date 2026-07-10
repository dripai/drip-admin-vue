from pydantic import BaseModel,Field
class JobSaveRequest(BaseModel):
 job_name:str=Field(alias="jobName");cron_expression:str=Field(alias="cronExpression");executor_type:str=Field(alias="executorType");script_file:str=Field(alias="scriptFile");script_args:str|None=Field(None,alias="scriptArgs");status:int|None=None;remark:str|None=None
