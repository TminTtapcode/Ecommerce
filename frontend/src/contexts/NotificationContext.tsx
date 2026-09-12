import { useEffect, useState, useCallback, type ReactNode } from 'react';
import { useAuth } from './AuthContext';
import { websocketService } from '../services/websocket';
import { notificationApi } from '../api/notificationApi';
import type { NotificationResponse } from '../api/types/notification.types';
import { NotificationContext as Context } from '../hooks/useNotifications';

export function NotificationProvider({ children }: { children: ReactNode }) {
  const { isAuthenticated } = useAuth();
  const [notifications, setNotifications] = useState<NotificationResponse[]>([]);
  const [unreadCount, setUnreadCount] = useState<number>(0);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [error, setError] = useState<string>('');

  // Tải danh sách thông báo và số lượng chưa đọc từ API
  const refresh = useCallback(async () => {
    if (!isAuthenticated) return;
    setIsLoading(true);
    setError('');
    try {
      const [listRes, countRes] = await Promise.all([
        notificationApi.getNotifications(0, 10),
        notificationApi.getUnreadCount(),
      ]);
      setNotifications(listRes.data.data?.content ?? []);
      setUnreadCount(countRes.data.data?.unreadCount ?? 0);
    } catch (err) {
      console.error('Lỗi khi tải thông báo:', err);
      setError('Không tải được thông báo. Vui lòng thử lại.');
    } finally {
      setIsLoading(false);
    }
  }, [isAuthenticated]);

  // Đánh dấu đã đọc thông báo (nếu không truyền id -> đánh dấu đọc tất cả)
  const read = useCallback(
    async (id?: number): Promise<boolean> => {
      try {
        if (id === undefined) {
          await notificationApi.markAllAsRead();
          setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
          setUnreadCount(0);
        } else {
          await notificationApi.markAsRead(id);
          setNotifications((prev) =>
            prev.map((n) => (n.id === id ? { ...n, isRead: true } : n))
          );
          setUnreadCount((prev) => Math.max(0, prev - 1));
        }
        return true;
      } catch (err) {
        console.error('Lỗi khi đánh dấu đã đọc:', err);
        return false;
      }
    },
    []
  );

  // Khi đăng nhập: tự động tải thông báo và đăng ký nhận thông báo realtime qua WebSocket
  useEffect(() => {
    if (!isAuthenticated) {
      setNotifications([]);
      setUnreadCount(0);
      return;
    }

    void refresh();

    // Lắng nghe thông báo đẩy từ WebSocket STOMP
    const unsubscribe = websocketService.subscribeNotifications((newNotification) => {
      setNotifications((prev) => [newNotification, ...prev]);
      setUnreadCount((prev) => prev + 1);
    });

    return () => {
      unsubscribe();
    };
  }, [isAuthenticated, refresh]);

  return (
    <Context.Provider
      value={{
        notifications,
        unreadCount,
        isLoading,
        error,
        refresh,
        read,
      }}
    >
      {children}
    </Context.Provider>
  );
}
