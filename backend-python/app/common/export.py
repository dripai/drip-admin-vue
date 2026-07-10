from collections.abc import Callable, Mapping, Sequence
from dataclasses import dataclass
from io import BytesIO
from typing import Any, Generic, TypeVar

from openpyxl import Workbook
from openpyxl.styles import Font

from app.common.errors import BusinessError, bad_request

T = TypeVar("T")


@dataclass(frozen=True)
class ExportColumnRequest:
    key: str
    title: str


@dataclass(frozen=True)
class ExportColumn(Generic[T]):
    value: Callable[[T], Any]


class ExcelExportService:
    def export(
        self,
        rows: Sequence[T],
        max_rows: int,
        selected_columns: Sequence[ExportColumnRequest],
        allowed_columns: Mapping[str, ExportColumn[T]],
    ) -> bytes:
        if max_rows <= 0:
            raise BusinessError(500000, "excel export max rows invalid")
        if len(rows) > max_rows:
            raise bad_request("excel export rows exceed limit")
        columns = self._resolve_columns(selected_columns, allowed_columns)

        workbook = Workbook(write_only=False)
        sheet = workbook.active
        sheet.title = "data"
        sheet.append([title for title, _ in columns])
        for cell in sheet[1]:
            cell.font = Font(name="SimSun", size=12, bold=True)
        sheet.row_dimensions[1].height = 24
        for row in rows:
            sheet.append([column.value(row) for _, column in columns])

        output = BytesIO()
        workbook.save(output)
        return output.getvalue()

    @staticmethod
    def _resolve_columns(
        selected_columns: Sequence[ExportColumnRequest],
        allowed_columns: Mapping[str, ExportColumn[T]],
    ) -> list[tuple[str, ExportColumn[T]]]:
        if not selected_columns:
            raise bad_request("export columns required")
        if len(selected_columns) > 100:
            raise bad_request("export columns size must be <= 100")
        seen: set[str] = set()
        resolved: list[tuple[str, ExportColumn[T]]] = []
        for request in selected_columns:
            key = request.key.strip()
            title = " ".join(request.title.split())
            if not key:
                raise bad_request("column key is required")
            if key in seen:
                raise bad_request(f"duplicate export column: {key}")
            if key not in allowed_columns:
                raise bad_request(f"unsupported export column: {key}")
            if not title:
                raise bad_request("column title is required")
            if len(title) > 64:
                raise bad_request("column title length must be <= 64")
            seen.add(key)
            resolved.append((title, allowed_columns[key]))
        return resolved
