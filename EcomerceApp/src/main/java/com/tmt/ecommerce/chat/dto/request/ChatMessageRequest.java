package com.tmt.ecommerce.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatMessageRequest {
    @NotNull(message = "Room ID is required")
    private Long roomId;

    @NotBlank(message = "Content is required")
    private String content;
}
