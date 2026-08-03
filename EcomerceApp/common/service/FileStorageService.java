// Đường dẫn: com.tmt.ecommerce.common.service.FileStorageService.java
package com.tmt.ecommerce.common.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface FileStorageService {
    String uploadImage(MultipartFile file) throws IOException;
    void deleteImage(String imageUrl) throws IOException;
}