package com.drip.admin.modules.system.controller;

import com.drip.admin.common.log.OperationLog;
import com.drip.admin.common.response.ApiResponse;
import com.drip.admin.common.security.RequirePermission;
import com.drip.admin.modules.system.service.FileService;
import com.drip.admin.modules.system.vo.FileUploadVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/system")
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/files")
    @RequirePermission("system:file:upload")
    @OperationLog(module = "文件上传", action = "上传文件")
    public ApiResponse<FileUploadVo> upload(@RequestParam("file") MultipartFile file) throws IOException {
        return ApiResponse.success(fileService.upload(file));
    }
}
