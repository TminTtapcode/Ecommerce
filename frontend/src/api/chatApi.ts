import apiClient from './apiClient';
import type { ApiResponse, PageResponse } from './types/common.types';
import type { ChatRoom, ChatMessage, CreateChatRoomRequest } from './types/chat.types';

export const chatApi = {
  createOrGetRoom: (data: CreateChatRoomRequest) => {
    return apiClient.post<ApiResponse<ChatRoom>>('/api/v1/chat/rooms', data);
  },

  getMyRooms: () => {
    return apiClient.get<ApiResponse<ChatRoom[]>>('/api/v1/chat/rooms');
  },

  getMessages: (roomId: number, page: number = 0, size: number = 50) => {
    return apiClient.get<ApiResponse<PageResponse<ChatMessage>>>(`/api/v1/chat/rooms/${roomId}/messages`, {
      params: { page, size, sort: 'createdAt,desc' },
    });
  },

  markAsRead: (roomId: number) => {
    return apiClient.put<ApiResponse<void>>(`/api/v1/chat/rooms/${roomId}/read`);
  },
};
