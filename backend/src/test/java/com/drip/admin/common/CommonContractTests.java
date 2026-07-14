package com.drip.admin.common;

import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.common.exception.GlobalExceptionHandler;
import com.drip.admin.common.export.ExcelExportService;
import com.drip.admin.common.export.ExportColumn;
import com.drip.admin.common.export.ExportColumnRequest;
import com.drip.admin.common.response.ApiResponse;
import com.drip.admin.config.JacksonConfig;
import com.drip.admin.modules.system.dto.MenuSaveRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CommonContractTests {
    @Test
    void jacksonSerializesLongIdsAsStrings() throws Exception {
        record LongIdPayload(Long id, long parentId) {}
        JacksonConfig jacksonConfig = new JacksonConfig();

        ObjectMapper objectMapper = jacksonConfig.objectMapper(jacksonConfig.longAsStringModule());
        String json = objectMapper.writeValueAsString(new LongIdPayload(1781234567890123456L, 1781234567890123457L));

        assertTrue(json.contains("\"id\":\"1781234567890123456\""));
        assertTrue(json.contains("\"parentId\":\"1781234567890123457\""));
    }

    @Test
    void jackson3SerializesLongIdsAsStringsForMvcResponses() throws Exception {
        record LongIdPayload(Long id, long parentId) {}
        JacksonConfig jacksonConfig = new JacksonConfig();
        tools.jackson.databind.json.JsonMapper.Builder builder = tools.jackson.databind.json.JsonMapper.builder();

        jacksonConfig.jsonMapperBuilderCustomizer(jacksonConfig.jackson3LongAsStringModule()).customize(builder);
        String json = builder.build().writeValueAsString(new LongIdPayload(1781234567890123456L, 1781234567890123457L));

        assertTrue(json.contains("\"id\":\"1781234567890123456\""));
        assertTrue(json.contains("\"parentId\":\"1781234567890123457\""));
    }

    @Test
    void jacksonAcceptsMenuParentIdsAsStringOrNumber() throws Exception {
        String payload = "{\"parentId\":\"2070959730788388865\",\"name\":\"菜单管理\",\"type\":\"MENU\"}";
        JacksonConfig jacksonConfig = new JacksonConfig();
        ObjectMapper jackson2 = jacksonConfig.objectMapper(jacksonConfig.longAsStringModule());
        tools.jackson.databind.json.JsonMapper.Builder builder = tools.jackson.databind.json.JsonMapper.builder();
        jacksonConfig.jsonMapperBuilderCustomizer(jacksonConfig.jackson3LongAsStringModule()).customize(builder);
        var jackson3 = builder.build();

        assertEquals(2070959730788388865L, jackson2.readValue(payload, MenuSaveRequest.class).getParentId());
        assertEquals(2070959730788388865L, jackson3.readValue(payload, MenuSaveRequest.class).getParentId());
        assertEquals(0L, jackson2.readValue("{\"parentId\":0,\"name\":\"菜单管理\",\"type\":\"MENU\"}", MenuSaveRequest.class).getParentId());
        assertEquals(0L, jackson3.readValue("{\"parentId\":0,\"name\":\"菜单管理\",\"type\":\"MENU\"}", MenuSaveRequest.class).getParentId());
    }

    @Test
    void mvcJsonConverterSerializesLongIdsAsStringsInApiResponses() throws Exception {
        record LongIdPayload(Long id, long parentId) {}
        JacksonConfig jacksonConfig = new JacksonConfig();
        tools.jackson.databind.json.JsonMapper.Builder builder = tools.jackson.databind.json.JsonMapper.builder();
        jacksonConfig.jsonMapperBuilderCustomizer(jacksonConfig.jackson3LongAsStringModule()).customize(builder);
        org.springframework.http.converter.json.JacksonJsonHttpMessageConverter converter =
            new org.springframework.http.converter.json.JacksonJsonHttpMessageConverter(builder);
        org.springframework.mock.http.MockHttpOutputMessage output = new org.springframework.mock.http.MockHttpOutputMessage();

        converter.write(ApiResponse.success(List.of(new LongIdPayload(1781234567890123456L, 1781234567890123457L))),
            org.springframework.http.MediaType.APPLICATION_JSON, output);
        String json = output.getBodyAsString();

        assertTrue(json.contains("\"id\":\"1781234567890123456\""));
        assertTrue(json.contains("\"parentId\":\"1781234567890123457\""));
    }

    @Test
    void commonPackageDoesNotDependOnSystemOrInfrastructureImplementations() throws Exception {
        List<Path> violations = new ArrayList<>();

        try (var paths = Files.walk(Path.of("src/main/java/com/drip/admin/common"))) {
            for (Path path : paths.filter(path -> path.toString().endsWith(".java")).toList()) {
                String source = Files.readString(path);
                if (source.contains("com.drip.admin.modules.") || source.contains("com.drip.admin.infrastructure.")) {
                    violations.add(path);
                }
            }
        }

        assertEquals(List.of(), violations);
    }

    @Test
    void excelExportRejectsRowsAboveCallerLimit() {
        ExcelExportService exportService = new ExcelExportService();
        ExportColumnRequest column = new ExportColumnRequest();
        column.setKey("name");
        column.setTitle("Name");

        BusinessException error = assertThrows(BusinessException.class, () -> exportService.export(
            new MockHttpServletResponse(),
            "demo",
            List.of("a", "b"),
            1,
            List.of(column),
            Map.of("name", ExportColumn.<String>of(value -> value))
        ));

        assertEquals(400000, error.code());
    }

    @Test
    void businessExceptionHttpStatusMatchesErrorCode() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        assertEquals(HttpStatus.UNAUTHORIZED, handler.business(new BusinessException(401000, "unauthorized")).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, handler.business(new BusinessException(403000, "forbidden")).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, handler.business(new BusinessException(404000, "not found")).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, handler.business(new BusinessException(409000, "conflict")).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, handler.business(new BusinessException(500000, "system")).getStatusCode());
    }

    @Test
    void globalExceptionHandlerIsRegisteredAsControllerAdvice() {
        assertTrue(GlobalExceptionHandler.class.isAnnotationPresent(RestControllerAdvice.class));
    }
}
