import { authSessionGuard } from '../contexts/authSession';
import { useState, useEffect, useCallback, useRef } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { websocketService } from '../services/websocket';
import { chatApi } from '../api/chatApi';
import type { ChatRoom, ChatMessage } from '../api/types/chat.types';

export const useChat = (initialRoomId?: number | null) => {
  const { user, session } = useAuth();
  const current = useCallback(() => !!session && authSessionGuard.accepts(session.generation) && localStorage.getItem('token') === session.token, [session]);
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

  useEffect(() => websocketService.onStatusChange(setIsConnected), []);
  useEffect(() => { setRooms([]); setMessages([]); setActiveRoomId(null); setIsLoadingRooms(false); setIsLoadingMessages(false); }, [session]);

  const loadRooms = useCallback(async () => {
    if (!session || !current()) return;
    setIsLoadingRooms(true);
    try {
      const res = await chatApi.getMyRooms(session!);
      if (current() && res.data.data) {
        setRooms(res.data.data);
      }
    } catch (err) {
      console.error('[useChat] Failed to load rooms:', err);
    } finally {
      if (current()) setIsLoadingRooms(false);
    }
  }, [session, current]);

  useEffect(() => {
    if (session) {
      loadRooms();
    }
  }, [session, loadRooms]);

  const loadMessages = useCallback(async (roomId: number) => {
    if (!session || !current()) return;
    setIsLoadingMessages(true);
    try {
      const res = await chatApi.getMessages(roomId, 0, 50, session);
      if (!current() || activeRoomIdRef.current !== roomId) return;
      if (res.data.data?.content) {

        setMessages([...res.data.data.content].reverse());
      }

      await chatApi.markAsRead(roomId, session!);
      if (!current()) return;

      setRooms((prev) =>
        prev.map((r) => (r.id === roomId ? { ...r, unreadCount: 0 } : r))
      );
    } catch (err) {
      console.error(`[useChat] Failed to load messages for room ${roomId}:`, err);
    } finally {
      if (current()) setIsLoadingMessages(false);
    }
  }, [session, current]);

  useEffect(() => {
    if (activeRoomId) {
      loadMessages(activeRoomId);
    } else {
      setMessages([]);
    }
  }, [activeRoomId, loadMessages]);

  useEffect(() => {
    const unsubMsg = websocketService.subscribe((incomingMsg: ChatMessage) => {
      if (!session || !current()) return;

      if (Number(incomingMsg.roomId) === Number(activeRoomIdRef.current)) {
        setMessages((prev) => {

          if (prev.some((m) => m.id === incomingMsg.id)) {
            return prev;
          }

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

        chatApi.markAsRead(incomingMsg.roomId, session!).catch(() => {});
      }

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
  }, [loadRooms, session, current]);

  const sendMessage = useCallback(
    (content: string, targetRoomId?: number) => {
      if (!current() || !websocketService.isConnected()) return false;
      const roomId = targetRoomId ?? activeRoomId;
      if (!roomId || !content.trim()) return false;

      const trimmed = content.trim();

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
    [activeRoomId, user?.userId, current]
  );

  const openChatWithShop = useCallback(
    async (shopId: number) => {
      if (!session || !current()) return null;
      try {
        const res = await chatApi.createOrGetRoom({ shopId }, session);
        if (current() && res.data.data) {
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
      } catch (err) {
        console.error('[useChat] Failed to open chat with shop:', err);
      }
      return null;
    },
    [session, current]
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
