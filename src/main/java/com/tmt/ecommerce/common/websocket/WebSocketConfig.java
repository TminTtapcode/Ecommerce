package com.tmt.ecommerce.common.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

/**
 * Cấu hình hệ thống WebSocket STOMP Message Broker.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtChannelInterceptor jwtChannelInterceptor;
    private final SocketSessions socketSessions;

    @Value("${application.websocket.allowed-origins:http://localhost:5173,http://127.0.0.1:5173}")
    private String[] allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Kênh broker gửi tin về client: /queue (tin riêng cho user), /topic (tin broadcast)
        registry.enableSimpleBroker("/queue", "/topic");

        // Tiền tố cho message từ client gửi lên Controller (@MessageMapping)
        registry.setApplicationDestinationPrefixes("/app");

        // Tiền tố gửi tin nhắn riêng theo user: /user/{userId}/queue/...
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Điểm kết nối bắt tay WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigins);

        // Trình xử lý lỗi STOMP: trả về frame ERROR rõ ràng cho client
        registry.setErrorHandler(new StompSubProtocolErrorHandler() {
            @Override
            public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {
                String errorCode = clientMessage == null || SimpMessageHeaderAccessor.getUser(clientMessage.getHeaders()) == null
                        ? "UNAUTHORIZED"
                        : "FORBIDDEN";

                for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
                    if ("UNAUTHORIZED".equals(cause.getMessage())) {
                        errorCode = "UNAUTHORIZED";
                        break;
                    }
                }

                StompHeaderAccessor headers = StompHeaderAccessor.create(StompCommand.ERROR);
                headers.setMessage(errorCode);
                headers.setNativeHeader("errorCode", errorCode);
                return MessageBuilder.createMessage(new byte[0], headers.getMessageHeaders());
            }
        });
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Gắn interceptor kiểm tra xác thực JWT cho luồng message đi vào
        registration.interceptors(jwtChannelInterceptor);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        // Kiểm tra phiên kết nối còn hợp lệ trước khi gửi tin nhắn outbound về cho client
        registration.interceptors(
                new ChannelInterceptor() {
                    @Override
                    public Message<?> preSend(Message<?> message, MessageChannel channel) {
                        return isSessionValid(message) ? message : null;
                    }
                },
                new ExecutorChannelInterceptor() {
                    @Override
                    public Message<?> beforeHandle(Message<?> message, MessageChannel channel, MessageHandler handler) {
                        return isSessionValid(message) ? message : null;
                    }
                }
        );
    }

    private boolean isSessionValid(Message<?> message) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(message);
        return headerAccessor.getMessageType() != SimpMessageType.MESSAGE || socketSessions.valid(headerAccessor.getSessionId());
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        // Theo dõi sự kiện mở và đóng phiên kết nối WebSocket để SocketSessions quản lý
        registration.addDecoratorFactory(handler -> new WebSocketHandlerDecorator(handler) {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                socketSessions.opened(session);
                super.afterConnectionEstablished(session);
            }

            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
                socketSessions.removed(session.getId());
                super.afterConnectionClosed(session, status);
            }
        });
    }
}
