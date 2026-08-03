package com.tmt.ecommerce.infrastructure.media.service;

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
    public String uploadImage(MultipartFile file) throws IOException {
        String publicId = UUID.randomUUID().toString();

        // Tạm thời bỏ "folder" đi để test quyền upload cơ bản
        Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "public_id", publicId
        ));

        return uploadResult.get("secure_url").toString();
    }

    @Override
    public void deleteImage(String imageUrl) throws IOException {
        // Tách publicId từ URL để gọi hàm xóa của Cloudinary (sẽ làm chi tiết sau nếu em cần)
    }
}