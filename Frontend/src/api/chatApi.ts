import type { RealtimeSession } from '../services/websocket';
import { sessionConfig } from './sessionRequest';
import apiClient from './apiClient';
import type { ApiResponse, PageResponse } from './types/common.types';
import type { ChatRoom, ChatMessage, CreateChatRoomRequest } from './types/chat.types';

export const chatApi = {
  createOrGetRoom: (data: CreateChatRoomRequest, session: RealtimeSession) => {
    return apiClient.post<ApiResponse<ChatRoom>>('/api/v1/chat/rooms', data, sessionConfig(session));
  },

  getMyRooms: (session: RealtimeSession) => {
    return apiClient.get<ApiResponse<ChatRoom[]>>('/api/v1/chat/rooms', sessionConfig(session));
  },

  getMessages: (roomId: number, page: number = 0, size: number, session: RealtimeSession) => {
    return apiClient.get<ApiResponse<PageResponse<ChatMessage>>>(`/api/v1/chat/rooms/${roomId}/messages`, {
      ...sessionConfig(session),
      params: { page, size, sort: 'createdAt,desc' }
    });
  },

  markAsRead: (roomId: number, session: RealtimeSession) => {
    return apiClient.put<ApiResponse<void>>(`/api/v1/chat/rooms/${roomId}/read`, null, sessionConfig(session));
  }
};
