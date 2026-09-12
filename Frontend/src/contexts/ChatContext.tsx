import React, { createContext, useContext, useState, useEffect, useCallback, useMemo } from 'react';
import { useAuth } from './AuthContext';
import { useChat } from '../hooks/useChat';
import type { ChatRoom, ChatMessage } from '../api/types/chat.types';

interface ChatContextType {
  isOpen: boolean;
  setIsOpen: (open: boolean) => void;
  openChat: () => void;
  closeChat: () => void;
  toggleChat: () => void;
  openChatWithShop: (shopId: number) => Promise<ChatRoom | null>;
  rooms: ChatRoom[];
  activeRoomId: number | null;
  setActiveRoomId: (id: number | null) => void;
  activeRoom: ChatRoom | null;
  messages: ChatMessage[];
  sendMessage: (content: string) => boolean;
  isLoadingRooms: boolean;
  isLoadingMessages: boolean;
  isConnected: boolean;
  totalUnreadCount: number;
  currentUserId: number | undefined;
  refreshRooms: () => void;
}

const ChatContext = createContext<ChatContextType | undefined>(undefined);

export const ChatProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuth();
  const [isOpen, setIsOpen] = useState<boolean>(false);

  const {
    isConnected,
    rooms,
    activeRoomId,
    setActiveRoomId,
    messages,
    isLoadingRooms,
    isLoadingMessages,
    loadRooms,
    sendMessage,
    openChatWithShop: chatOpenShop,
    currentUserId,
  } = useChat();

  const totalUnreadCount = useMemo(() => {
    return rooms.reduce((acc, r) => acc + (r.unreadCount || 0), 0);
  }, [rooms]);

  const activeRoom = useMemo(() => {
    return rooms.find((r) => r.id === activeRoomId) || null;
  }, [rooms, activeRoomId]);

  const openChat = useCallback(() => {
    setIsOpen(true);
  }, []);

  const closeChat = useCallback(() => {
    setIsOpen(false);
  }, []);

  const toggleChat = useCallback(() => {
    setIsOpen((prev) => !prev);
  }, []);

  const openChatWithShop = useCallback(
    async (shopId: number): Promise<ChatRoom | null> => {
      setIsOpen(true);
      const room = await chatOpenShop(shopId);
      if (room) {
        setActiveRoomId(room.id);
      }
      return room;
    },
    [chatOpenShop, setActiveRoomId]
  );

  useEffect(() => {
    if (!isAuthenticated) {
      setIsOpen(false);
    }
  }, [isAuthenticated]);

  return (
    <ChatContext.Provider
      value={{
        isOpen,
        setIsOpen,
        openChat,
        closeChat,
        toggleChat,
        openChatWithShop,
        rooms,
        activeRoomId,
        setActiveRoomId,
        activeRoom,
        messages,
        sendMessage,
        isLoadingRooms,
        isLoadingMessages,
        isConnected,
        totalUnreadCount,
        currentUserId,
        refreshRooms: loadRooms,
      }}
    >
      {children}
    </ChatContext.Provider>
  );
};

export const useChatContext = () => {
  const context = useContext(ChatContext);
  if (!context) {
    throw new Error('useChatContext must be used within a ChatProvider');
  }
  return context;
};
