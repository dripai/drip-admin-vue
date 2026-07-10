from datetime import datetime

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.common.errors import not_found
from app.common.pagination import PageQuery, PageResult
from app.modules.system.entity import SysLoginLog, SysOperationLog


class LogService:
    def __init__(self, db: AsyncSession) -> None:
        self.db = db

    async def login_logs(
        self,
        page: PageQuery,
        username: str | None,
        status: str | None,
        login_type: str | None,
        device_type: str | None,
        ip: str | None,
        login_from: datetime | None,
        login_to: datetime | None,
    ) -> PageResult[dict]:
        filters = []
        for column, value in (
            (SysLoginLog.username, username),
            (SysLoginLog.status, status),
            (SysLoginLog.login_type, login_type),
            (SysLoginLog.device_type, device_type),
            (SysLoginLog.ip, ip),
        ):
            if value and value.strip():
                filters.append(column.like(f"%{value.strip()}%"))
        if login_from is not None:
            filters.append(SysLoginLog.login_at >= login_from)
        if login_to is not None:
            filters.append(SysLoginLog.login_at <= login_to)
        total = await self.db.scalar(
            select(func.count()).select_from(SysLoginLog).where(*filters)
        ) or 0
        rows = (
            await self.db.scalars(
                select(SysLoginLog)
                .where(*filters)
                .order_by(SysLoginLog.login_at.desc(), SysLoginLog.id.desc())
                .offset((page.page - 1) * page.page_size)
                .limit(page.page_size)
            )
        ).all()
        return PageResult(
            list=[self._login_vo(row) for row in rows],
            total=str(total),
            page=page.page,
            pageSize=page.page_size,
        )

    async def login_log(self, log_id: int) -> dict:
        row = await self.db.scalar(select(SysLoginLog).where(SysLoginLog.id == log_id))
        if row is None:
            raise not_found()
        return self._login_vo(row)

    async def operation_logs(
        self,
        page: PageQuery,
        operator: str | None,
        module: str | None,
        action: str | None,
        status: str | None,
        path: str | None,
        created_from: datetime | None,
        created_to: datetime | None,
    ) -> PageResult[dict]:
        filters = []
        for column, value in (
            (SysOperationLog.operator_name, operator),
            (SysOperationLog.module, module),
            (SysOperationLog.action, action),
            (SysOperationLog.response_status, status),
            (SysOperationLog.path, path),
        ):
            if value and value.strip():
                filters.append(column.like(f"%{value.strip()}%"))
        if created_from is not None:
            filters.append(SysOperationLog.created_at >= created_from)
        if created_to is not None:
            filters.append(SysOperationLog.created_at <= created_to)
        total = await self.db.scalar(
            select(func.count()).select_from(SysOperationLog).where(*filters)
        ) or 0
        rows = (
            await self.db.scalars(
                select(SysOperationLog)
                .where(*filters)
                .order_by(SysOperationLog.created_at.desc(), SysOperationLog.id.desc())
                .offset((page.page - 1) * page.page_size)
                .limit(page.page_size)
            )
        ).all()
        return PageResult(
            list=[self._operation_vo(row) for row in rows],
            total=str(total),
            page=page.page,
            pageSize=page.page_size,
        )

    async def operation_log(self, log_id: int) -> dict:
        row = await self.db.scalar(select(SysOperationLog).where(SysOperationLog.id == log_id))
        if row is None:
            raise not_found()
        return self._operation_vo(row)

    @staticmethod
    def _login_vo(row: SysLoginLog) -> dict:
        return {
            "id": str(row.id),
            "userId": str(row.user_id) if row.user_id is not None else None,
            "username": row.username,
            "realName": row.real_name,
            "loginType": row.login_type,
            "status": row.status,
            "failureReason": row.failure_reason,
            "ip": row.ip,
            "userAgent": row.user_agent,
            "deviceType": row.device_type,
            "loginAt": row.login_at,
        }

    @staticmethod
    def _operation_vo(row: SysOperationLog) -> dict:
        return {
            "id": str(row.id),
            "operatorId": str(row.operator_id) if row.operator_id is not None else None,
            "operator": row.operator_name,
            "module": row.module,
            "action": row.action,
            "method": row.method,
            "path": row.path,
            "requestParams": row.request_params,
            "status": row.response_status,
            "errorMessage": row.error_message,
            "duration": str(row.cost_ms),
            "createdAt": row.created_at,
        }
