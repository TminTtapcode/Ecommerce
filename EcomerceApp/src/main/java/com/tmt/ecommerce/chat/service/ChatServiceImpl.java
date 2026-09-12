package com.tmt.ecommerce.chat.service;

import com.tmt.ecommerce.chat.dto.request.ChatMessageRequest;
import com.tmt.ecommerce.chat.dto.request.CreateChatRoomRequest;
import com.tmt.ecommerce.chat.dto.response.ChatMessageResponse;
import com.tmt.ecommerce.chat.dto.response.ChatRoomResponse;
import com.tmt.ecommerce.chat.entity.ChatMessage;
import com.tmt.ecommerce.chat.entity.ChatRoom;
import com.tmt.ecommerce.chat.exception.ChatAccessDeniedException;
import com.tmt.ecommerce.chat.exception.ChatRoomNotFoundException;
import com.tmt.ecommerce.chat.repository.ChatMessageRepository;
import com.tmt.ecommerce.chat.repository.ChatRoomRepository;
import com.tmt.ecommerce.shop.api.ShopInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ShopInternalService shopInternalService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public ChatRoomResponse createOrGetRoom(CreateChatRoomRequest request, Long buyerId) {
        Long shopId = request.getShopId();

        shopInternalService.getUserIdByShopId(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found"));

        ChatRoom room = chatRoomRepository.findByBuyerIdAndShopId(buyerId, shopId)
                .orElseGet(() -> {
                    ChatRoom newRoom = ChatRoom.builder()
                            .buyerId(buyerId)
                            .shopId(shopId)
                            .build();
                    return chatRoomRepository.save(newRoom);
                });

        return mapToChatRoomResponse(room, buyerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getMyRooms(Long userId) {
        List<ChatRoomResponse> responses = new ArrayList<>();

        chatRoomRepository.findAllByBuyerIdOrderByUpdatedAtDesc(userId)
            .forEach(room -> responses.add(mapToChatRoomResponse(room, userId)));

        shopInternalService.findShopIdByUserId(userId).ifPresent(shopId -> {
            chatRoomRepository.findAllByShopIdOrderByUpdatedAtDesc(shopId)
                .forEach(room -> responses.add(mapToChatRoomResponse(room, userId)));
        });

        responses.sort((r1, r2) -> {
            if (r1.getUpdatedAt() == null && r2.getUpdatedAt() == null) return 0;
            if (r1.getUpdatedAt() == null) return 1;
            if (r2.getUpdatedAt() == null) return -1;
            return r2.getUpdatedAt().compareTo(r1.getUpdatedAt());
        });

        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getMessages(Long roomId, Long userId, Pageable pageable) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException("Chat room not found"));

        validateAccess(room, userId);

        return chatMessageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, pageable)
                .map(this::mapToChatMessageResponse);
    }

    @Override
    @Transactional
    public void sendMessage(ChatMessageRequest request, Long senderId) {
        ChatRoom room = chatRoomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ChatRoomNotFoundException("Chat room not found"));

        validateAccess(room, senderId);

        ChatMessage message = ChatMessage.builder()
                .roomId(room.getId())
                .senderId(senderId)
                .content(request.getContent())
                .build();

        message = chatMessageRepository.save(message);

        room.setUpdatedAt(LocalDateTime.now());
        chatRoomRepository.save(room);

        Long recipientId = getRecipientId(room, senderId);

        ChatMessageResponse response = mapToChatMessageResponse(message);
        messagingTemplate.convertAndSendToUser(
                recipientId.toString(),
                "/queue/messages",
                response
        );
        if (!recipientId.equals(senderId)) {
            messagingTemplate.convertAndSendToUser(
                    senderId.toString(),
                    "/queue/messages",
                    response
            );
        }
    }

    @Override
    @Transactional
    public void markMessagesAsRead(Long roomId, Long userId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException("Chat room not found"));

        validateAccess(room, userId);

        chatMessageRepository.markAsReadByRoomIdAndNotSenderId(roomId, userId);
    }

    private void validateAccess(ChatRoom room, Long userId) {
        boolean isBuyer = room.getBuyerId().equals(userId);
        boolean isVendor = shopInternalService.getUserIdByShopId(room.getShopId())
                .filter(ownerId -> ownerId.equals(userId))
                .isPresent();

        if (!isBuyer && !isVendor) {
            throw new ChatAccessDeniedException("You do not have access to this chat room");
        }
    }

    private Long getRecipientId(ChatRoom room, Long senderId) {
        if (room.getBuyerId().equals(senderId)) {
            return shopInternalService.getUserIdByShopId(room.getShopId())
                    .orElseThrow(() -> new IllegalArgumentException("Shop owner not found"));
        } else {
            return room.getBuyerId();
        }
    }

    private ChatRoomResponse mapToChatRoomResponse(ChatRoom room, Long currentUserId) {
        String lastMessage = null;
        LocalDateTime lastMessageAt = null;

        var messageOpt = chatMessageRepository.findFirstByRoomIdOrderByCreatedAtDesc(room.getId());
        if (messageOpt.isPresent()) {
            lastMessage = messageOpt.get().getContent();
            lastMessageAt = messageOpt.get().getCreatedAt();
        }

        long unreadCount = chatMessageRepository.countByRoomIdAndSenderIdNotAndIsReadFalse(room.getId(), currentUserId);

        String shopName = shopInternalService.getShopNameById(room.getShopId())
                .orElse("Shop #" + room.getShopId());

        return ChatRoomResponse.builder()
                .id(room.getId())
                .buyerId(room.getBuyerId())
                .shopId(room.getShopId())
                .shopName(shopName)
                .lastMessage(lastMessage)
                .lastMessageAt(lastMessageAt)
                .unreadCount(unreadCount)
                .updatedAt(room.getUpdatedAt() != null ? room.getUpdatedAt() : room.getCreatedAt())
                .build();
    }

    private ChatMessageResponse mapToChatMessageResponse(ChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .roomId(message.getRoomId())
                .senderId(message.getSenderId())
                .content(message.getContent())
                .isRead(message.isRead())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
