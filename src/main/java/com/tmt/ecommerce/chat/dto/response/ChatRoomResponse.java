package com.tmt.ecommerce.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponse {
    private Long id;
    private Long buyerId;
    private Long shopId;
    private String shopName;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private Long unreadCount;
    private LocalDateTime updatedAt;
}
