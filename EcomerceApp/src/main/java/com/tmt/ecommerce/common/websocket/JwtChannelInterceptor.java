package com.tmt.ecommerce.common.websocket;

import com.tmt.ecommerce.common.security.*;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import java.security.Principal;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {
    private final JwtService jwt;
    private final UserDetailsService users;
    private final SocketSessions sessions;
    @Override public Message<?> preSend(Message<?> message, MessageChannel channel) {
        var a = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (a == null) throw new IllegalArgumentException("FORBIDDEN");
        var command = a.getCommand();
        if (command == StompCommand.CONNECT) {
            try {
                String header = a.getFirstNativeHeader("Authorization");
                a.setNativeHeader("Authorization", null);
                if (header == null || !header.startsWith("Bearer ")) throw new IllegalArgumentException();
                String token = header.substring(7);
                var details = users.loadUserByUsername(jwt.extractUsername(token));
                Long id = jwt.extractUserId(token);
                if (!(details instanceof CurrentUserPrincipal current) || id == null || id <= 0
                        || !id.equals(current.getId()) || !jwt.isTokenValid(token, details)) throw new IllegalArgumentException();
                sessions.authenticate(a.getSessionId(), jwt.extractClaim(token, Claims::getExpiration).getTime());
                Principal principal = () -> id.toString();
                a.setUser(new UsernamePasswordAuthenticationToken(principal, null, details.getAuthorities()));
            } catch (Exception failure) { throw new IllegalArgumentException("UNAUTHORIZED"); }
            return message;
        }
        if (command == StompCommand.DISCONNECT || command == StompCommand.UNSUBSCRIBE || command == null) return message;
        if (a.getUser() == null || !sessions.valid(a.getSessionId())) throw new IllegalArgumentException("UNAUTHORIZED");
        if (command == StompCommand.SUBSCRIBE && Set.of("/user/queue/messages", "/user/queue/notifications").contains(a.getDestination())) return message;
        if (command == StompCommand.SEND && "/app/chat.send".equals(a.getDestination())) return message;
        throw new IllegalArgumentException("FORBIDDEN");
    }
}
