package com.tmt.ecommerce.infrastructure.media.controller;

import com.tmt.ecommerce.common.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaController {

    private final FileStorageService fileStorageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // 1. Validate file rỗng
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File không được để trống"));
            }

            // 2. Validate định dạng (Tùy chọn: chỉ cho phép ảnh)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Chỉ hỗ trợ upload file hình ảnh (JPEG, PNG,...)"));
            }

            // 3. Gọi service để đẩy lên Cloudinary
            String imageUrl = fileStorageService.uploadImage(file);

            // 4. Trả về chuẩn JSON để Frontend dễ parse
            return ResponseEntity.ok(Map.of(
                    "message", "Upload thành công",
                    "url", imageUrl
            ));

        } catch (Exception e) {
            // Log lỗi ra console để debug (thực tế nên dùng @Slf4j)
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi trong quá trình upload: " + e.getMessage()));
        }
    }
}