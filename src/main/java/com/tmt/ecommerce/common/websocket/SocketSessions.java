package com.tmt.ecommerce.common.websocket;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Quản lý vòng đời của các phiên kết nối WebSocket STOMP.
 * Tự động ngắt kết nối WebSocket khi JWT Token hết hạn.
 */
@Slf4j
@Component
public class SocketSessions {

    private static final class SessionState {
        final WebSocketSession socket;
        volatile long expiresAt;
        ScheduledFuture<?> expiryTask;

        SessionState(WebSocketSession socket) {
            this.socket = socket;
        }
    }

    private final ConcurrentMap<String, SessionState> activeSessions = new ConcurrentHashMap<>();
    private final ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1, runnable -> {
        Thread thread = new Thread(runnable, "websocket-session-expiry");
        thread.setDaemon(true);
        return thread;
    });

    public SocketSessions() {
        scheduler.setRemoveOnCancelPolicy(true);
    }

    /**
     * Ghi nhận khi một kết nối WebSocket mới được mở
     */
    public void opened(WebSocketSession socket) {
        activeSessions.put(socket.getId(), new SessionState(socket));
    }

    /**
     * Xác thực phiên kết nối với thời điểm hết hạn của JWT Token
     */
    public void authenticate(String sessionId, long expiresAt) {
        SessionState state = activeSessions.get(sessionId);
        if (state == null) {
            throw new IllegalArgumentException("UNAUTHORIZED");
        }

        synchronized (state) {
            if (state.expiresAt != 0 || expiresAt <= System.currentTimeMillis()) {
                throw new IllegalArgumentException("UNAUTHORIZED");
            }
            state.expiresAt = expiresAt;
            long delay = expiresAt - System.currentTimeMillis();
            state.expiryTask = scheduler.schedule(() -> close(sessionId), delay, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Kiểm tra phiên kết nối có còn hợp lệ và chưa hết hạn hay không
     */
    public boolean valid(String sessionId) {
        if (sessionId == null) {
            return false;
        }
        SessionState state = activeSessions.get(sessionId);
        return state != null && state.expiresAt > System.currentTimeMillis() && state.socket.isOpen();
    }

    /**
     * Đóng kết nối khi JWT hết hạn hoặc vi phạm chính sách
     */
    public void close(String sessionId) {
        SessionState state = activeSessions.get(sessionId);
        if (state != null) {
            try {
                state.socket.close(CloseStatus.POLICY_VIOLATION);
            } catch (IOException ignored) {
            }
            removed(sessionId);
        }
    }

    /**
     * Dọn dẹp phiên khi kết nối đã đóng
     */
    public void removed(String sessionId) {
        SessionState state = activeSessions.remove(sessionId);
        if (state != null) {
            synchronized (state) {
                if (state.expiryTask != null) {
                    state.expiryTask.cancel(false);
                }
            }
        }
    }

    @PreDestroy
    public void stop() {
        activeSessions.keySet().forEach(this::close);
        scheduler.shutdownNow();
    }
}
