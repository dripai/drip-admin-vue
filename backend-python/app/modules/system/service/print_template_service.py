from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.common.errors import BusinessError, not_found
from app.common.id import new_id
from app.common.pagination import PageQuery, PageResult
from app.modules.system.dto.print_template_request import PrintTemplateCopyRequest, PrintTemplateSaveRequest
from app.modules.system.entity import SysPrintTemplate


class PrintTemplateService:
    def __init__(self, db: AsyncSession) -> None:
        self.db = db

    async def list_templates(
        self,
        page: PageQuery,
        code: str | None,
        name: str | None,
        status: int | None,
    ) -> PageResult[dict]:
        filters = [SysPrintTemplate.deleted == 0]
        if code and code.strip():
            filters.append(SysPrintTemplate.code.like(f"%{code.strip()}%"))
        if name and name.strip():
            filters.append(SysPrintTemplate.name.like(f"%{name.strip()}%"))
        if status is not None:
            filters.append(SysPrintTemplate.status == status)
        total = await self.db.scalar(
            select(func.count()).select_from(SysPrintTemplate).where(*filters)
        ) or 0
        rows = (
            await self.db.scalars(
                select(SysPrintTemplate)
                .where(*filters)
                .order_by(SysPrintTemplate.updated_at.desc(), SysPrintTemplate.id.desc())
                .offset((page.page - 1) * page.page_size)
                .limit(page.page_size)
            )
        ).all()
        return PageResult(
            list=[self._vo(row) for row in rows],
            total=str(total),
            page=page.page,
            pageSize=page.page_size,
        )

    async def detail(self, template_id: int) -> dict:
        return self._vo(await self._find(template_id))

    async def save(self, template_id: int | None, request: PrintTemplateSaveRequest) -> str | None:
        await self._assert_code_available(request.code.strip(), template_id)
        if template_id is None:
            row = SysPrintTemplate(
                id=new_id(),
                code=request.code.strip(),
                name=request.name.strip(),
                paper_type=request.paper_type.strip(),
                template_json=request.template_json,
                status=request.status,
                deleted=0,
            )
            self.db.add(row)
            await self.db.commit()
            return str(row.id)
        row = await self._find(template_id)
        row.code = request.code.strip()
        row.name = request.name.strip()
        row.paper_type = request.paper_type.strip()
        row.template_json = request.template_json
        row.status = request.status
        await self.db.commit()
        return None

    async def copy(self, template_id: int, request: PrintTemplateCopyRequest) -> str:
        source = await self._find(template_id)
        await self._assert_code_available(request.code.strip(), None)
        row = SysPrintTemplate(
            id=new_id(),
            code=request.code.strip(),
            name=request.name.strip(),
            paper_type=source.paper_type,
            template_json=source.template_json,
            status=request.status,
            deleted=0,
        )
        self.db.add(row)
        await self.db.commit()
        return str(row.id)

    async def delete(self, template_id: int) -> None:
        row = await self._find(template_id)
        row.deleted = 1
        await self.db.commit()

    async def update_status(self, template_id: int, status: int | None) -> None:
        row = await self._find(template_id)
        row.status = 1 if status is None else status
        await self.db.commit()

    async def _find(self, template_id: int) -> SysPrintTemplate:
        row = await self.db.scalar(
            select(SysPrintTemplate).where(
                SysPrintTemplate.id == template_id,
                SysPrintTemplate.deleted == 0,
            )
        )
        if row is None:
            raise not_found("print template not found")
        return row

    async def _assert_code_available(self, code: str, template_id: int | None) -> None:
        query = select(SysPrintTemplate.id).where(
            SysPrintTemplate.code == code,
            SysPrintTemplate.deleted == 0,
        )
        if template_id is not None:
            query = query.where(SysPrintTemplate.id != template_id)
        if await self.db.scalar(query) is not None:
            raise BusinessError(409000, "print template code already exists")

    @staticmethod
    def _vo(row: SysPrintTemplate) -> dict:
        return {
            "id": str(row.id),
            "code": row.code,
            "name": row.name,
            "paperType": row.paper_type,
            "templateJson": row.template_json,
            "status": row.status,
            "createdAt": row.created_at,
            "updatedAt": row.updated_at,
        }
