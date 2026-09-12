package com.tmt.ecommerce.common.websocket;

import com.tmt.ecommerce.common.security.CurrentUserPrincipal;
import com.tmt.ecommerce.common.security.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Set;

/**
 * Interceptor kiểm tra xác thực JWT Token và phân quyền đích đến cho các gói tin STOMP.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final SocketSessions socketSessions;

    private static final Set<String> ALLOWED_SUBSCRIBE_DESTINATIONS = Set.of(
            "/user/queue/messages",
            "/user/queue/notifications"
    );

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            throw new IllegalArgumentException("FORBIDDEN");
        }

        StompCommand command = accessor.getCommand();

        // 1. Xử lý bắt tay kết nối STOMP (CONNECT): Xác thực JWT Token
        if (StompCommand.CONNECT.equals(command)) {
            try {
                String authHeader = accessor.getFirstNativeHeader("Authorization");
                accessor.setNativeHeader("Authorization", null);
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    throw new IllegalArgumentException("UNAUTHORIZED");
                }

                String token = authHeader.substring(7);
                String username = jwtService.extractUsername(token);
                Long userId = jwtService.extractUserId(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (!(userDetails instanceof CurrentUserPrincipal currentUser)
                        || userId == null
                        || userId <= 0
                        || !userId.equals(currentUser.getId())
                        || !jwtService.isTokenValid(token, userDetails)) {
                    throw new IllegalArgumentException("UNAUTHORIZED");
                }

                // Ghi nhận thời gian hết hạn của token vào SocketSessions
                long expiresAt = jwtService.extractClaim(token, Claims::getExpiration).getTime();
                socketSessions.authenticate(accessor.getSessionId(), expiresAt);

                // Gán Principal để SimpMessagingTemplate có thể gửi tin nhắn đích danh cho user
                Principal principal = () -> String.valueOf(userId);
                accessor.setUser(new UsernamePasswordAuthenticationToken(principal, null, userDetails.getAuthorities()));

                log.info("[WebSocket] Người dùng #{} kết nối STOMP thành công", userId);
            } catch (Exception e) {
                log.warn("[WebSocket] Từ chối kết nối STOMP: {}", e.getMessage());
                throw new IllegalArgumentException("UNAUTHORIZED");
            }
            return message;
        }

        // 2. Bỏ qua các lệnh hệ thống DISCONNECT, UNSUBSCRIBE
        if (StompCommand.DISCONNECT.equals(command) || StompCommand.UNSUBSCRIBE.equals(command) || command == null) {
            return message;
        }

        // 3. Với các thao tác khác (SUBSCRIBE, SEND), kiểm tra xác thực và tính hợp lệ của phiên
        if (accessor.getUser() == null || !socketSessions.valid(accessor.getSessionId())) {
            throw new IllegalArgumentException("UNAUTHORIZED");
        }

        // 4. Kiểm tra phân quyền kênh đăng ký (SUBSCRIBE)
        if (StompCommand.SUBSCRIBE.equals(command) && ALLOWED_SUBSCRIBE_DESTINATIONS.contains(accessor.getDestination())) {
            return message;
        }

        // 5. Kiểm tra đích gửi tin nhắn (SEND)
        if (StompCommand.SEND.equals(command) && "/app/chat.send".equals(accessor.getDestination())) {
            return message;
        }

        // 6. Các frame không được hỗ trợ (BEGIN, COMMIT, ABORT, ACK, NACK,...) hoặc đích trái phép bị từ chối
        throw new IllegalArgumentException("FORBIDDEN");
    }
}
