package com.tmt.ecommerce.chat.service;

import com.tmt.ecommerce.chat.dto.request.ChatMessageRequest;
import com.tmt.ecommerce.chat.dto.request.CreateChatRoomRequest;
import com.tmt.ecommerce.chat.dto.response.ChatMessageResponse;
import com.tmt.ecommerce.chat.dto.response.ChatRoomResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChatService {
    ChatRoomResponse createOrGetRoom(CreateChatRoomRequest request, Long buyerId);
    List<ChatRoomResponse> getMyRooms(Long userId);
    Page<ChatMessageResponse> getMessages(Long roomId, Long userId, Pageable pageable);
    void sendMessage(ChatMessageRequest request, Long senderId);
    void markMessagesAsRead(Long roomId, Long userId);
}
