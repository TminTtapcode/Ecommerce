export interface ChatRoom {
  id: number;
  buyerId: number;
  shopId: number;
  shopName?: string | null;
  lastMessage?: string | null;
  lastMessageAt?: string | null;
  unreadCount?: number;
  updatedAt: string;
}

export interface ChatMessage {
  id: number;
  roomId: number;
  senderId: number;
  content: string;
  isRead: boolean;
  createdAt: string;
}

export interface SendMessageRequest {
  roomId: number;
  content: string;
}

export interface CreateChatRoomRequest {
  shopId: number;
}
