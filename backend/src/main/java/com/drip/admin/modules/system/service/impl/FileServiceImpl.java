package com.drip.admin.modules.system.service.impl;

import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.modules.system.service.ConfigService;
import com.drip.admin.modules.system.service.FileService;
import com.drip.admin.modules.system.vo.FileUploadVo;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class FileServiceImpl implements FileService {
    private final ConfigService configService;

    public FileServiceImpl(ConfigService configService) {
        this.configService = configService;
    }

    @Override
    public FileUploadVo upload(MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new BusinessException(400000, "file must not be empty");
        if (file.getSize() > configService.requiredLong("upload.maxSizeBytes")) throw new BusinessException(400000, "file exceeds max upload size");
        if (!allowedExtensions().contains(fileExtension(file.getOriginalFilename()))) throw new BusinessException(400000, "file extension is not allowed");
        return new FileUploadVo("local-" + System.currentTimeMillis(), "", file.getOriginalFilename(), file.getSize());
    }

    private List<String> allowedExtensions() {
        return Arrays.stream(configService.requiredValue("upload.allowedExtensions").split(","))
            .map(String::trim)
            .map(value -> value.startsWith(".") ? value.substring(1) : value)
            .map(String::toLowerCase)
            .filter(value -> !value.isBlank())
            .toList();
    }

    private static String fileExtension(String filename) {
        if (filename == null) return "";
        int index = filename.lastIndexOf('.');
        if (index < 0 || index == filename.length() - 1) return "";
        return filename.substring(index + 1).toLowerCase();
    }
}
