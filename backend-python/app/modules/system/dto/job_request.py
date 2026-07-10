from pydantic import BaseModel, Field


class JobSaveRequest(BaseModel):
    job_name: str = Field(alias="jobName", min_length=1, max_length=128)
    cron_expression: str = Field(alias="cronExpression", min_length=1, max_length=64)
    executor_type: str = Field(alias="executorType", pattern="^(python|shell|bat|powershell|ps1)$")
    script_file: str = Field(alias="scriptFile", min_length=1, max_length=255)
    script_args: str | None = Field(default=None, alias="scriptArgs", max_length=1024)
    status: int | None = Field(default=None, ge=0, le=1)
    remark: str | None = Field(default=None, max_length=255)
