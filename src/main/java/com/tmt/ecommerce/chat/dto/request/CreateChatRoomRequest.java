package com.tmt.ecommerce.chat.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateChatRoomRequest {
    @NotNull(message = "Shop ID is required")
    private Long shopId;
}
