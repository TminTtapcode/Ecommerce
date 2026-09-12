import apiClient from './apiClient';
import type { ApiResponse, PageResponse } from './types/common.types';
import type { NotificationResponse, UnreadCountResponse } from './types/notification.types';

export const notificationApi = {
  getNotifications: (page: number = 0, size: number = 10) =>
    apiClient.get<ApiResponse<PageResponse<NotificationResponse>>>('/api/v1/notifications', {
      params: { page, size },
    }),

  getUnreadCount: () =>
    apiClient.get<ApiResponse<UnreadCountResponse>>('/api/v1/notifications/unread-count'),

  markAsRead: (id: number) =>
    apiClient.put<ApiResponse<void>>(`/api/v1/notifications/${id}/read`),

  markAllAsRead: () =>
    apiClient.put<ApiResponse<void>>('/api/v1/notifications/read-all'),
};
