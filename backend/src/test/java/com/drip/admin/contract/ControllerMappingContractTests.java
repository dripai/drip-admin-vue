package com.drip.admin.contract;

import com.drip.admin.common.response.ApiResponse;
import com.drip.admin.modules.system.controller.*;
import com.drip.admin.modules.system.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static com.drip.admin.support.TestSupport.assertMapping;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ControllerMappingContractTests {
    @Test
    void healthEndpointReturnsUpStatus() {
        ApiResponse<com.drip.admin.modules.system.vo.HealthVo> response = new HealthController().health();

        assertEquals(0, response.code());
        assertEquals("UP", response.data().status());
    }

    @Test
    void rootEndpointRedirectsToSwaggerAndFaviconReturnsNoContent() {
        RootController controller = new RootController();

        assertEquals(HttpStatus.FOUND, controller.root().getStatusCode());
        assertEquals("swagger-ui/index.html", controller.root().getHeaders().getFirst("Location"));
        assertEquals(HttpStatus.NO_CONTENT, controller.favicon().getStatusCode());
    }

    @Test
    void systemControllersUseSystemBasePathAndCamelCaseUrls() throws Exception {
        assertEquals("/system", AuthController.class.getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class).value()[0]);
        assertMapping(UserController.class, "resetPassword", org.springframework.web.bind.annotation.PostMapping.class, "/user/{id}/resetPassword", long.class, PasswordResetRequest.class);
        assertMapping(SystemLogController.class, "loginLogs", org.springframework.web.bind.annotation.GetMapping.class, "/loginLog", LoginLogQuery.class);
        assertMapping(SystemLogController.class, "operationLogs", org.springframework.web.bind.annotation.GetMapping.class, "/operationLog", OperationLogQuery.class);
        assertMapping(OnlineUserController.class, "onlineUsers", org.springframework.web.bind.annotation.GetMapping.class, "/onlineUser", OnlineUserQuery.class);
        assertMapping(JobController.class, "jobLogs", org.springframework.web.bind.annotation.GetMapping.class, "/job/{id}/runLog", long.class, JobRunLogQuery.class);
        assertMapping(JobController.class, "jobRunLogs", org.springframework.web.bind.annotation.GetMapping.class, "/jobRunLog", JobRunLogQuery.class);
        assertEquals("/system/print-template", PrintTemplateController.class.getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class).value()[0]);
        assertMapping(PrintTemplateController.class, "status", org.springframework.web.bind.annotation.PutMapping.class, "/{id}/status", long.class, StatusUpdateRequest.class);
    }
}
