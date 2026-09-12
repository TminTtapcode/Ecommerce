package com.tmt.ecommerce.common.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.config.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.*;
import org.springframework.web.socket.*;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtChannelInterceptor inbound;
    private final SocketSessions sessions;
    @Value("${application.websocket.allowed-origins:http://localhost:5173,http://127.0.0.1:5173}") String[] origins;
    @Override public void configureMessageBroker(MessageBrokerRegistry c) {
        c.enableSimpleBroker("/queue", "/topic"); c.setApplicationDestinationPrefixes("/app"); c.setUserDestinationPrefix("/user");
    }
    @Override public void registerStompEndpoints(StompEndpointRegistry r) {
        r.setErrorHandler(new StompSubProtocolErrorHandler() {
            @Override public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> message, Throwable ex) {
                String code = message == null || org.springframework.messaging.simp.SimpMessageHeaderAccessor.getUser(message.getHeaders()) == null ? "UNAUTHORIZED" : "FORBIDDEN";
                for (Throwable cause = ex; cause != null; cause = cause.getCause())
                    if ("UNAUTHORIZED".equals(cause.getMessage())) code = "UNAUTHORIZED";
                var h = StompHeaderAccessor.create(StompCommand.ERROR);
                h.setMessage(code); h.setNativeHeader("errorCode", code);
                return MessageBuilder.createMessage(new byte[0], h.getMessageHeaders());
            }
        });
        r.addEndpoint("/ws").setAllowedOrigins(origins);
    }
    @Override public void configureClientInboundChannel(ChannelRegistration c) { c.interceptors(inbound); }
    @Override public void configureClientOutboundChannel(ChannelRegistration c) {
        c.interceptors(new ChannelInterceptor() {
            @Override public Message<?> preSend(Message<?> message, MessageChannel channel) { return allowed(message) ? message : null; }
        }, new ExecutorChannelInterceptor() {
            @Override public Message<?> beforeHandle(Message<?> message, MessageChannel channel, MessageHandler handler) {
                return allowed(message) ? message : null;
            }
        });
    }
    private boolean allowed(Message<?> message) {
        var h = org.springframework.messaging.simp.SimpMessageHeaderAccessor.wrap(message);
        return h.getMessageType() != SimpMessageType.MESSAGE || sessions.valid(h.getSessionId());
    }
    @Override public void configureWebSocketTransport(WebSocketTransportRegistration r) {
        r.addDecoratorFactory(handler -> new WebSocketHandlerDecorator(handler) {
            @Override public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                sessions.opened(session); super.afterConnectionEstablished(session);
            }
            @Override public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
                sessions.removed(session.getId()); super.afterConnectionClosed(session, status);
            }
        });
    }
}
