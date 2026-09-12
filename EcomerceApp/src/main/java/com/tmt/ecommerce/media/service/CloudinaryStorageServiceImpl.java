package com.tmt.ecommerce.media.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.tmt.ecommerce.common.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudinaryStorageServiceImpl implements FileStorageService {

    private final Cloudinary cloudinary;

    @Override
    public FileStorageResponse uploadImage(MultipartFile file) throws IOException {
        String publicId = "products/" + UUID.randomUUID().toString();

        Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "public_id", publicId
        ));

        String url = uploadResult.get("secure_url").toString();
        String returnedPublicId = uploadResult.containsKey("public_id") ? uploadResult.get("public_id").toString() : publicId;

        return new FileStorageResponse(url, returnedPublicId);
    }

    @Override
    public void deleteImage(String publicId) throws IOException {
        if (publicId == null || publicId.isBlank()) return;
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new IOException("Không thể xóa ảnh trên Cloudinary: " + e.getMessage(), e);
        }
    }
}
