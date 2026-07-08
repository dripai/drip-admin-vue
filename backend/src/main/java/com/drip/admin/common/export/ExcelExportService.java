package com.drip.admin.common.export;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.constant.OrderConstant;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.handler.RowWriteHandler;
import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;
import com.alibaba.excel.write.handler.context.RowWriteHandlerContext;
import com.drip.admin.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

@Service
public class ExcelExportService {
    private static final GlobalHeadStyleHandler GLOBAL_HEAD_STYLE_HANDLER = new GlobalHeadStyleHandler();

    public <T> void export(
            HttpServletResponse response,
            String fileName,
            List<T> rows,
            List<ExportColumnRequest> selectedColumns,
            Map<String, ExportColumn<T>> allowedColumns) {
        List<ResolvedColumn<T>> columns = resolveColumns(selectedColumns, allowedColumns);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodeFileName(fileName));
        try {
            EasyExcel.write(response.getOutputStream())
                .head(columns.stream().map(column -> List.of(column.title())).toList())
                .registerWriteHandler(GLOBAL_HEAD_STYLE_HANDLER)
                .sheet("数据")
                .doWrite(rows.stream().map(row -> toExportRow(row, columns)).toList());
        } catch (IOException ex) {
            throw new BusinessException(500000, "excel export failed");
        }
    }

    private static <T> List<ResolvedColumn<T>> resolveColumns(List<ExportColumnRequest> selectedColumns, Map<String, ExportColumn<T>> allowedColumns) {
        if (selectedColumns == null || selectedColumns.isEmpty()) throw new BusinessException(400000, "导出列不能为空");
        Set<String> keys = new HashSet<>();
        List<ResolvedColumn<T>> resolved = new ArrayList<>();
        for (ExportColumnRequest request : selectedColumns) {
            String key = trim(request.getKey());
            if (!keys.add(key)) throw new BusinessException(400000, "导出列重复: " + key);
            ExportColumn<T> column = allowedColumns.get(key);
            if (column == null) throw new BusinessException(400000, "导出列不支持: " + key);
            resolved.add(new ResolvedColumn<>(sanitizeTitle(request.getTitle()), column));
        }
        return resolved;
    }

    private static <T> List<Object> toExportRow(T row, List<ResolvedColumn<T>> columns) {
        return columns.stream().map(column -> column.column().value(row)).toList();
    }

    private static String sanitizeTitle(String value) {
        String title = trim(value).replaceAll("[\\r\\n\\t]", " ");
        if (title.isBlank()) throw new BusinessException(400000, "导出列标题不能为空");
        if (title.length() > 64) throw new BusinessException(400000, "导出列标题过长");
        return title;
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String encodeFileName(String fileName) {
        String normalized = fileName.endsWith(".xlsx") ? fileName : fileName + ".xlsx";
        return URLEncoder.encode(normalized, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private record ResolvedColumn<T>(String title, ExportColumn<T> column) {}

    private static class GlobalHeadStyleHandler implements CellWriteHandler, RowWriteHandler {
        private static final short HEAD_FONT_SIZE = 12;
        private static final float HEAD_ROW_HEIGHT = 24F;
        private static final String HEAD_FONT_NAME = "宋体";

        @Override
        public int order() {
            return OrderConstant.FILL_STYLE + 1;
        }

        @Override
        public void afterCellDispose(CellWriteHandlerContext context) {
            if (!Boolean.TRUE.equals(context.getHead())) return;
            Cell cell = context.getCell();
            if (cell == null) return;
            Workbook workbook = cell.getSheet().getWorkbook();
            CellStyle style = workbook.createCellStyle();
            style.cloneStyleFrom(cell.getCellStyle());
            style.setWrapText(false);

            Font font = workbook.createFont();
            font.setFontName(HEAD_FONT_NAME);
            font.setFontHeightInPoints(HEAD_FONT_SIZE);
            font.setBold(true);
            style.setFont(font);
            cell.setCellStyle(style);
        }

        @Override
        public void afterRowDispose(RowWriteHandlerContext context) {
            if (!Boolean.TRUE.equals(context.getHead()) || context.getRow() == null) return;
            context.getRow().setHeightInPoints(HEAD_ROW_HEIGHT);
        }
    }
}
