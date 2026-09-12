package com.tmt.ecommerce.notification.dto.response;

import java.time.LocalDateTime;

public record NotificationResponse(
    Long id,
    String type,
    String title,
    String message,
    String referenceId,
    boolean isRead,
    LocalDateTime createdAt
) {}
