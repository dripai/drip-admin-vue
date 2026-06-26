package com.drip.admin.modules.system.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface FileService {
    Map<String, Object> upload(MultipartFile file) throws IOException;
}
