package com.tmt.ecommerce.common.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface FileStorageService {
    FileStorageResponse uploadImage(MultipartFile file) throws IOException;
    void deleteImage(String publicId) throws IOException;

    record FileStorageResponse(String url, String publicId) {}
}
