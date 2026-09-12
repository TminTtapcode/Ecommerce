package com.tmt.ecommerce.chat.controller;

import com.tmt.ecommerce.chat.dto.request.ChatMessageRequest;
import com.tmt.ecommerce.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatService chatService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Validated ChatMessageRequest request, Principal principal) {
        Long senderId = Long.parseLong(principal.getName());
        chatService.sendMessage(request, senderId);
    }
}
