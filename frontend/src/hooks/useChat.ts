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

  // Load rooms
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

  // Load messages when activeRoomId changes
  const loadMessages = useCallback(async (roomId: number) => {
    if (!session || !current()) return;
    setIsLoadingMessages(true);
    try {
      const res = await chatApi.getMessages(roomId, 0, 50, session);
      if (!current() || activeRoomIdRef.current !== roomId) return;
      if (res.data.data?.content) {
        // Reverse because backend returns descending order
        setMessages([...res.data.data.content].reverse());
      }
      // Mark as read
      await chatApi.markAsRead(roomId, session!);
      if (!current()) return;
      // Update room unread count locally
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

  // Listen to incoming messages
  useEffect(() => {
    const unsubMsg = websocketService.subscribe((incomingMsg: ChatMessage) => {
      if (!session || !current()) return;
      // If it's for the currently active room, append or replace optimistic message
      if (Number(incomingMsg.roomId) === Number(activeRoomIdRef.current)) {
        setMessages((prev) => {
          // Avoid duplicate if already present with same real id
          if (prev.some((m) => m.id === incomingMsg.id)) {
            return prev;
          }
          // If it matches an optimistic message sent by me, replace it
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

        // Mark as read if received while room is active
        chatApi.markAsRead(incomingMsg.roomId, session!).catch(() => {});
      }

      // Update rooms list last message and bump to top
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
          // If new room, reload room list
          loadRooms();
          return prev;
        }
      });
    });

    return () => {
      unsubMsg();
    };
  }, [loadRooms, session, current]);

  // Send message
  const sendMessage = useCallback(
    (content: string, targetRoomId?: number) => {
      if (!current() || !websocketService.isConnected()) return false;
      const roomId = targetRoomId ?? activeRoomId;
      if (!roomId || !content.trim()) return false;

      const trimmed = content.trim();

      // Optimistically append message to active conversation immediately
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

        // Optimistically update conversation preview in left sidebar
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

  // Open / create room with a shop
  const openChatWithShop = useCallback(
    async (shopId: number) => {
      if (!session || !current()) return null;
      try {
        const res = await chatApi.createOrGetRoom({ shopId }, session);
        if (current() && res.data.data) {
          const room = res.data.data;
          setActiveRoomId(room.id);
          // Prepend or select in rooms list
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
