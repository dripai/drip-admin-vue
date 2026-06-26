package com.drip.admin.modules.system.service;

import com.drip.admin.modules.system.vo.FileUploadVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {
    FileUploadVo upload(MultipartFile file) throws IOException;
}
