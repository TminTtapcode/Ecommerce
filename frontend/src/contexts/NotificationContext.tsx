import React, { createContext, useContext, useEffect, useState, useCallback, type ReactNode } from 'react';
import { useAuth } from './AuthContext';
import { websocketService } from '../services/websocket';
import { notificationApi } from '../api/notificationApi';
import type { NotificationResponse } from '../api/types/notification.types';

export interface NotificationContextType {
  notifications: NotificationResponse[];
  unreadCount: number;
  isLoading: boolean;
  error: string;
  refresh: () => void;
  read: (id?: number) => Promise<boolean>;
}

export const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

export const NotificationProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuth();
  const [notifications, setNotifications] = useState<NotificationResponse[]>([]);
  const [unreadCount, setUnreadCount] = useState<number>(0);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [error, setError] = useState<string>('');

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
      console.error(err);
      setError('Không tải được thông báo. Vui lòng thử lại.');
    } finally {
      setIsLoading(false);
    }
  }, [isAuthenticated]);

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
        console.error(err);
        return false;
      }
    },
    []
  );

  useEffect(() => {
    if (!isAuthenticated) {
      setNotifications([]);
      setUnreadCount(0);
      return;
    }

    void refresh();

    const unsubscribe = websocketService.subscribeNotifications((newNotification) => {
      setNotifications((prev) => [newNotification, ...prev]);
      setUnreadCount((prev) => prev + 1);
    });

    return () => {
      unsubscribe();
    };
  }, [isAuthenticated, refresh]);

  return (
    <NotificationContext.Provider
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
    </NotificationContext.Provider>
  );
};

export const useNotifications = () => {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error('useNotifications must be used within a NotificationProvider');
  }
  return context;
};
