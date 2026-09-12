package com.tmt.ecommerce.common.websocket;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import java.util.concurrent.*;

@Component
public class SocketSessions {
    private static final class State {
        final WebSocketSession socket;
        volatile long expiresAt;
        ScheduledFuture<?> expiry;
        State(WebSocketSession socket) { this.socket = socket; }
    }
    private final ConcurrentMap<String, State> sessions = new ConcurrentHashMap<>();
    private final ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1, r -> {
        Thread t = new Thread(r, "websocket-expiry"); t.setDaemon(true); return t;
    });
    public SocketSessions() { scheduler.setRemoveOnCancelPolicy(true); }
    public void opened(WebSocketSession socket) { sessions.put(socket.getId(), new State(socket)); }
    public void authenticate(String id, long expiresAt) {
        State s = sessions.get(id);
        if (s == null) throw new IllegalArgumentException("UNAUTHORIZED");
        synchronized (s) {
            if (s.expiresAt != 0 || expiresAt <= System.currentTimeMillis()) throw new IllegalArgumentException("UNAUTHORIZED");
            s.expiresAt = expiresAt;
            s.expiry = scheduler.schedule(() -> close(id), expiresAt - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }
    }
    public boolean valid(String id) {
        State s = id == null ? null : sessions.get(id);
        return s != null && s.expiresAt > System.currentTimeMillis() && s.socket.isOpen();
    }
    public void close(String id) {
        State s = sessions.get(id);
        if (s != null) {
            try { s.socket.close(CloseStatus.POLICY_VIOLATION); } catch (java.io.IOException ignored) { }
            removed(id);
        }
    }
    public void removed(String id) {
        State s = sessions.remove(id);
        if (s != null) synchronized (s) { if (s.expiry != null) s.expiry.cancel(false); }
    }
    @PreDestroy void stop() { sessions.keySet().forEach(this::close); scheduler.shutdownNow(); }
}
