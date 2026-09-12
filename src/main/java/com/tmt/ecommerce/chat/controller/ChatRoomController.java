package com.tmt.ecommerce.chat.controller;

import com.tmt.ecommerce.chat.dto.request.CreateChatRoomRequest;
import com.tmt.ecommerce.chat.dto.response.ChatMessageResponse;
import com.tmt.ecommerce.chat.dto.response.ChatRoomResponse;
import com.tmt.ecommerce.chat.service.ChatService;
import com.tmt.ecommerce.common.annotation.CurrentUserId;
import com.tmt.ecommerce.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatService chatService;

    @PostMapping("/rooms")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createOrGetRoom(
            @Valid @RequestBody CreateChatRoomRequest request,
            @CurrentUserId Long userId) {
        ChatRoomResponse room = chatService.createOrGetRoom(request, userId);
        return ResponseEntity.ok(ApiResponse.<ChatRoomResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Tạo hoặc lấy phòng chat thành công")
                .data(room)
                .build());
    }

    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getMyRooms(
            @CurrentUserId Long userId) {
        List<ChatRoomResponse> rooms = chatService.getMyRooms(userId);
        return ResponseEntity.ok(ApiResponse.<List<ChatRoomResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách phòng chat thành công")
                .data(rooms)
                .build());
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<Page<ChatMessageResponse>>> getMessages(
            @PathVariable Long roomId,
            Pageable pageable,
            @CurrentUserId Long userId) {
        Page<ChatMessageResponse> messages = chatService.getMessages(roomId, userId, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<ChatMessageResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy lịch sử tin nhắn thành công")
                .data(messages)
                .build());
    }

    @PutMapping("/rooms/{roomId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long roomId,
            @CurrentUserId Long userId) {
        chatService.markMessagesAsRead(roomId, userId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Đã đánh dấu đã đọc")
                .build());
    }
}
