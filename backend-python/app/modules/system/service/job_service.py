import asyncio
from datetime import datetime

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.common.errors import bad_request, not_found
from app.common.id import new_id
from app.common.pagination import PageQuery, PageResult
from app.infrastructure.script_executor import ScriptExecutor
from app.modules.system.dto.job_request import JobSaveRequest
from app.modules.system.entity import SysJob, SysJobRunLog


class JobService:
    def __init__(self, db: AsyncSession, script_dir: str) -> None:
        self.db = db
        self.executor = ScriptExecutor(script_dir)

    async def list_jobs(
        self,
        page: PageQuery,
        job_name: str | None,
        remark: str | None,
        status: int | None,
        created_at: str | None,
    ) -> PageResult[dict]:
        filters = [SysJob.deleted == 0]
        if job_name and job_name.strip():
            filters.append(SysJob.job_name.like(f"%{job_name.strip()}%"))
        if remark and remark.strip():
            filters.append(SysJob.remark.like(f"%{remark.strip()}%"))
        if status is not None:
            filters.append(SysJob.status == status)
        if created_at and created_at.strip():
            filters.append(SysJob.created_at.like(f"%{created_at.strip()}%"))
        total = await self.db.scalar(select(func.count()).select_from(SysJob).where(*filters)) or 0
        rows = (
            await self.db.scalars(
                select(SysJob)
                .where(*filters)
                .order_by(SysJob.created_at.desc(), SysJob.id.desc())
                .offset((page.page - 1) * page.page_size)
                .limit(page.page_size)
            )
        ).all()
        return PageResult(
            list=[self._job_vo(row) for row in rows],
            total=str(total),
            page=page.page,
            pageSize=page.page_size,
        )

    async def detail(self, job_id: int) -> dict:
        return self._job_vo(await self._find(job_id))

    async def save(self, job_id: int | None, request: JobSaveRequest) -> str | None:
        self._validate_cron(request.cron_expression)
        self.executor.validate_script_file(request.executor_type, request.script_file)
        values = {
            "job_name": request.job_name.strip(),
            "cron_expression": request.cron_expression.strip(),
            "executor_type": request.executor_type.strip().lower(),
            "script_file": request.script_file.strip(),
            "script_args": _optional(request.script_args),
            "status": 1 if request.status is None else request.status,
            "remark": _optional(request.remark),
        }
        if job_id is None:
            row = SysJob(id=new_id(), deleted=0, **values)
            self.db.add(row)
            await self.db.commit()
            return str(row.id)
        row = await self._find(job_id)
        for name, value in values.items():
            setattr(row, name, value)
        await self.db.commit()
        return None

    async def delete(self, job_id: int) -> None:
        row = await self._find(job_id)
        row.deleted = 1
        await self.db.commit()

    async def update_status(self, job_id: int, status: int | None) -> None:
        row = await self._find(job_id)
        row.status = 1 if status is None else status
        await self.db.commit()

    async def run(self, job_id: int) -> None:
        job = await self._find(job_id)
        started = datetime.now()
        run_log = SysJobRunLog(
            id=new_id(),
            job_id=job.id,
            job_name=job.job_name,
            status="RUNNING",
            started_at=started,
            finished_at=None,
            cost_ms=0,
            error_message=None,
        )
        self.db.add(run_log)
        await self.db.commit()
        try:
            await asyncio.to_thread(
                self.executor.execute,
                job.executor_type,
                job.script_file or "",
                job.script_args,
            )
            run_log.status = "SUCCESS"
        except Exception as exc:
            run_log.status = "FAIL"
            run_log.error_message = str(exc)[:512]
        run_log.finished_at = datetime.now()
        run_log.cost_ms = int((run_log.finished_at - started).total_seconds() * 1000)
        await self.db.commit()

    async def run_logs(
        self,
        page: PageQuery,
        job_id: int | None,
        job_name: str | None,
        status: str | None,
        started_range: list[str] | None,
    ) -> PageResult[dict]:
        filters = []
        if job_id is not None:
            filters.append(SysJobRunLog.job_id == job_id)
        if job_name and job_name.strip():
            filters.append(SysJobRunLog.job_name.like(f"%{job_name.strip()}%"))
        if status and status.strip():
            filters.append(SysJobRunLog.status.like(f"%{status.strip()}%"))
        if started_range:
            if len(started_range) != 2:
                raise bad_request("startedRange must contain start and end")
            try:
                start, end = (datetime.fromisoformat(value) for value in started_range)
            except ValueError as exc:
                raise bad_request("startedRange is invalid") from exc
            filters.extend([SysJobRunLog.started_at >= start, SysJobRunLog.started_at <= end])
        total = await self.db.scalar(
            select(func.count()).select_from(SysJobRunLog).where(*filters)
        ) or 0
        rows = (
            await self.db.scalars(
                select(SysJobRunLog)
                .where(*filters)
                .order_by(SysJobRunLog.started_at.desc(), SysJobRunLog.id.desc())
                .offset((page.page - 1) * page.page_size)
                .limit(page.page_size)
            )
        ).all()
        return PageResult(
            list=[self._run_log_vo(row) for row in rows],
            total=str(total),
            page=page.page,
            pageSize=page.page_size,
        )

    async def _find(self, job_id: int) -> SysJob:
        row = await self.db.scalar(select(SysJob).where(SysJob.id == job_id, SysJob.deleted == 0))
        if row is None:
            raise not_found()
        return row

    @staticmethod
    def _validate_cron(expression: str) -> None:
        count = len(expression.strip().split())
        if count < 5 or count > 7:
            raise bad_request("cronExpression format is invalid")

    @staticmethod
    def _job_vo(row: SysJob) -> dict:
        return {
            "id": str(row.id),
            "jobName": row.job_name,
            "cronExpression": row.cron_expression,
            "executorType": row.executor_type,
            "scriptFile": row.script_file,
            "scriptArgs": row.script_args,
            "status": row.status,
            "remark": row.remark,
            "createdAt": row.created_at,
            "updatedAt": row.updated_at,
        }

    @staticmethod
    def _run_log_vo(row: SysJobRunLog) -> dict:
        return {
            "id": str(row.id),
            "jobId": str(row.job_id),
            "jobName": row.job_name,
            "status": row.status,
            "startedAt": row.started_at,
            "finishedAt": row.finished_at,
            "costMs": str(row.cost_ms),
            "errorMessage": row.error_message,
        }


def _optional(value: str | None) -> str | None:
    return value.strip() if value and value.strip() else None
