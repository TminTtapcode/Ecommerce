import apiClient from './apiClient';
import type { ApiResponse, PageResponse } from './types/common.types';
import type { NotificationResponse, UnreadCountResponse } from './types/notification.types';
import type { RealtimeSession } from '../services/websocket';
import { sessionConfig } from './sessionRequest';
export const notificationApi = {
  getNotifications: (session: RealtimeSession) => apiClient.get<ApiResponse<PageResponse<NotificationResponse>>>('/api/v1/notifications', { ...sessionConfig(session), params: { page: 0, size: 10 } }),
  getUnreadCount: (session: RealtimeSession) => apiClient.get<ApiResponse<UnreadCountResponse>>('/api/v1/notifications/unread-count', sessionConfig(session)),
  markAsRead: (session: RealtimeSession, id: number) => apiClient.put(`/api/v1/notifications/${id}/read`, null, sessionConfig(session)),
  markAllAsRead: (session: RealtimeSession) => apiClient.put('/api/v1/notifications/read-all', null, sessionConfig(session)),
};
