package com.tmt.ecommerce.media.controller;

import com.tmt.ecommerce.common.service.FileStorageService;
import com.tmt.ecommerce.common.dto.ApiResponse;
import com.tmt.ecommerce.media.dto.UploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaController {

    private final FileStorageService fileStorageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {

            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.<UploadResponse>builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("File không được để trống")
                        .build());
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(ApiResponse.<UploadResponse>builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Chỉ hỗ trợ upload file hình ảnh (JPEG, PNG,...)")
                        .build());
            }

            FileStorageService.FileStorageResponse storageResponse = fileStorageService.uploadImage(file);

            return ResponseEntity.ok(ApiResponse.<UploadResponse>builder()
                    .status(HttpStatus.OK.value())
                    .message("Upload thành công")
                    .data(new UploadResponse(storageResponse.url(), storageResponse.publicId()))
                    .build());

        } catch (Exception e) {

            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<UploadResponse>builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Lỗi trong quá trình upload: " + e.getMessage())
                            .build());
        }
    }
}
