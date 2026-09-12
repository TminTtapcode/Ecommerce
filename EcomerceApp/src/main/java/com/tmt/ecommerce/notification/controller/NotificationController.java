package com.tmt.ecommerce.notification.controller;

import com.tmt.ecommerce.common.dto.ApiResponse;
import com.tmt.ecommerce.common.annotation.CurrentUserId;
import com.tmt.ecommerce.notification.dto.response.NotificationResponse;
import com.tmt.ecommerce.notification.dto.response.UnreadCountResponse;
import com.tmt.ecommerce.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getUserNotifications(
            @CurrentUserId Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<NotificationResponse> notifications = notificationService.getUserNotifications(userId, page, size);
        return ResponseEntity.ok(ApiResponse.<Page<NotificationResponse>>builder()
                .status(200)
                .message("Lấy danh sách thông báo thành công")
                .data(notifications)
                .build());
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount(@CurrentUserId Long userId) {
        UnreadCountResponse countResponse = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.<UnreadCountResponse>builder()
                .status(200)
                .message("Lấy số lượng thông báo chưa đọc thành công")
                .data(countResponse)
                .build());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@CurrentUserId Long userId, @PathVariable Long id) {
        notificationService.markAsRead(userId, id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200)
                .message("Đã đánh dấu đọc thông báo")
                .build());
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@CurrentUserId Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200)
                .message("Đã đánh dấu đọc tất cả thông báo")
                .build());
    }
}
