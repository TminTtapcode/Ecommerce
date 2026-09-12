import { useState, useEffect, useCallback, useRef } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { websocketService } from '../services/websocket';
import { chatApi } from '../api/chatApi';
import type { ChatRoom, ChatMessage } from '../api/types/chat.types';

export const useChat = (initialRoomId?: number | null) => {
  const { user, isAuthenticated } = useAuth();
  const [isConnected, setIsConnected] = useState(websocketService.isConnected());
  const [rooms, setRooms] = useState<ChatRoom[]>([]);
  const [activeRoomId, setActiveRoomId] = useState<number | null>(initialRoomId ?? null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [isLoadingRooms, setIsLoadingRooms] = useState(false);
  const [isLoadingMessages, setIsLoadingMessages] = useState(false);

  const activeRoomIdRef = useRef<number | null>(activeRoomId);
  useEffect(() => {
    activeRoomIdRef.current = activeRoomId;
  }, [activeRoomId]);

  // Cập nhật trạng thái kết nối socket
  useEffect(() => {
    return websocketService.onStatusChange(setIsConnected);
  }, []);

  // Reset dữ liệu khi đăng xuất
  useEffect(() => {
    if (!isAuthenticated) {
      setRooms([]);
      setMessages([]);
      setActiveRoomId(null);
      setIsLoadingRooms(false);
      setIsLoadingMessages(false);
    }
  }, [isAuthenticated]);

  // Tải danh sách phòng chat
  const loadRooms = useCallback(async () => {
    if (!isAuthenticated) return;
    setIsLoadingRooms(true);
    try {
      const res = await chatApi.getMyRooms();
      if (res.data.data) {
        setRooms(res.data.data);
      }
    } catch (err) {
      console.error('[useChat] Lỗi khi tải danh sách phòng chat:', err);
    } finally {
      setIsLoadingRooms(false);
    }
  }, [isAuthenticated]);

  useEffect(() => {
    if (isAuthenticated) {
      loadRooms();
    }
  }, [isAuthenticated, loadRooms]);

  // Tải lịch sử tin nhắn của một phòng chat
  const loadMessages = useCallback(
    async (roomId: number) => {
      if (!isAuthenticated) return;
      setIsLoadingMessages(true);
      try {
        const res = await chatApi.getMessages(roomId, 0, 50);
        if (activeRoomIdRef.current !== roomId) return;
        if (res.data.data?.content) {
          setMessages([...res.data.data.content].reverse());
        }

        // Đánh dấu đã đọc tin nhắn trong phòng
        await chatApi.markAsRead(roomId);
        setRooms((prev) =>
          prev.map((r) => (r.id === roomId ? { ...r, unreadCount: 0 } : r))
        );
      } catch (err) {
        console.error(`[useChat] Lỗi khi tải tin nhắn phòng ${roomId}:`, err);
      } finally {
        setIsLoadingMessages(false);
      }
    },
    [isAuthenticated]
  );

  useEffect(() => {
    if (activeRoomId) {
      loadMessages(activeRoomId);
    } else {
      setMessages([]);
    }
  }, [activeRoomId, loadMessages]);

  // Lắng nghe tin nhắn mới từ WebSocket STOMP
  useEffect(() => {
    if (!isAuthenticated) return;

    const unsubMsg = websocketService.subscribe((incomingMsg: ChatMessage) => {
      if (Number(incomingMsg.roomId) === Number(activeRoomIdRef.current)) {
        setMessages((prev) => {
          if (prev.some((m) => m.id === incomingMsg.id)) {
            return prev;
          }
          // Thay thế tin nhắn tạm (optimistic message) nếu có
          const optIndex = prev.findIndex(
            (m) => m.id < 0 && m.senderId === incomingMsg.senderId && m.content === incomingMsg.content
          );
          if (optIndex !== -1) {
            const next = [...prev];
            next[optIndex] = incomingMsg;
            return next;
          }
          return [...prev, incomingMsg];
        });

        // Báo cho backend biết đã đọc tin nhắn mới này
        chatApi.markAsRead(incomingMsg.roomId).catch(() => {});
      }

      // Cập nhật tin nhắn cuối cùng và số lượng chưa đọc cho danh sách phòng
      setRooms((prev) => {
        const existing = prev.find((r) => Number(r.id) === Number(incomingMsg.roomId));
        if (existing) {
          const updated: ChatRoom = {
            ...existing,
            lastMessage: incomingMsg.content,
            lastMessageAt: incomingMsg.createdAt,
            unreadCount:
              Number(existing.id) === Number(activeRoomIdRef.current)
                ? 0
                : (existing.unreadCount ?? 0) + 1,
            updatedAt: incomingMsg.createdAt,
          };
          return [updated, ...prev.filter((r) => Number(r.id) !== Number(incomingMsg.roomId))];
        } else {
          loadRooms();
          return prev;
        }
      });
    });

    return () => {
      unsubMsg();
    };
  }, [isAuthenticated, loadRooms]);

  // Gửi tin nhắn
  const sendMessage = useCallback(
    (content: string, targetRoomId?: number) => {
      if (!isAuthenticated || !websocketService.isConnected()) return false;
      const roomId = targetRoomId ?? activeRoomId;
      if (!roomId || !content.trim()) return false;

      const trimmed = content.trim();

      // Optimistic UI: Hiển thị tin nhắn ngay lập tức trên màn hình
      if (roomId === activeRoomId && user?.userId) {
        const optimisticMsg: ChatMessage = {
          id: -Date.now(),
          roomId,
          senderId: user.userId,
          content: trimmed,
          isRead: false,
          createdAt: new Date().toISOString(),
        };
        setMessages((prev) => [...prev, optimisticMsg]);

        setRooms((prev) => {
          const existing = prev.find((r) => Number(r.id) === Number(roomId));
          if (!existing) return prev;
          const updated: ChatRoom = {
            ...existing,
            lastMessage: trimmed,
            lastMessageAt: optimisticMsg.createdAt,
            updatedAt: optimisticMsg.createdAt,
          };
          return [updated, ...prev.filter((r) => Number(r.id) !== Number(roomId))];
        });
      }

      return websocketService.sendMessage({
        roomId,
        content: trimmed,
      });
    },
    [activeRoomId, user?.userId, isAuthenticated]
  );

  // Mở hoặc tạo phòng chat với Shop
  const openChatWithShop = useCallback(
    async (shopId: number) => {
      if (!isAuthenticated) return null;
      try {
        const res = await chatApi.createOrGetRoom({ shopId });
        if (res.data.data) {
          const room = res.data.data;
          setActiveRoomId(room.id);
          setRooms((prev) => {
            if (prev.some((r) => r.id === room.id)) {
              return prev;
            }
            return [room, ...prev];
          });
          return room;
        }
      } catch (err: any) {
        const errorMsg =
          err?.response?.data?.message || 'Không thể tạo hoặc mở phòng chat với shop này.';
        alert(errorMsg);
        console.error('[useChat] Lỗi khi tạo phòng chat với shop:', err);
      }
      return null;
    },
    [isAuthenticated]
  );

  return {
    isConnected,
    rooms,
    activeRoomId,
    setActiveRoomId,
    messages,
    isLoadingRooms,
    isLoadingMessages,
    loadRooms,
    loadMessages,
    sendMessage,
    openChatWithShop,
    currentUserId: user?.userId,
  };
};
