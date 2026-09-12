export interface NotificationResponse {
  id: number; type: string; title: string; message: string; referenceId: string | null;
  isRead: boolean; createdAt: string;
}
export interface UnreadCountResponse { unreadCount: number }
