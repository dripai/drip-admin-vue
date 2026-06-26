package com.drip.admin.modules.system.service.impl;

import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.modules.system.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class FileServiceImpl implements FileService {
    private final long maxUploadSize;
    private final List<String> allowedUploadTypes;

    public FileServiceImpl(
        @Value("${drip.upload.max-size-bytes}") long maxUploadSize,
        @Value("${drip.upload.allowed-types}") String allowedUploadTypes
    ) {
        this.maxUploadSize = maxUploadSize;
        this.allowedUploadTypes = Arrays.stream(allowedUploadTypes.split(","))
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .toList();
    }

    @Override
    public Map<String, Object> upload(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new BusinessException(400000, "file must not be empty");
        }
        if (file.getSize() > maxUploadSize) {
            throw new BusinessException(400000, "file exceeds max upload size");
        }
        if (!allowedUploadTypes.contains(file.getContentType())) {
            throw new BusinessException(400000, "file content type is not allowed");
        }
        return Map.of(
            "fileId", "local-" + System.currentTimeMillis(),
            "url", "",
            "fileName", file.getOriginalFilename(),
            "size", file.getSize()
        );
    }
}
