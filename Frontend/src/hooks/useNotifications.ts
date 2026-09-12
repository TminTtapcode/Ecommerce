import { createContext, useContext } from 'react';
import type { NotificationResponse } from '../api/types/notification.types';
export type NotificationView = { notifications: NotificationResponse[]; unreadCount: number; isLoading: boolean; error: string };
export const NotificationContext = createContext<(NotificationView & { refresh: () => void; read: (id?: number) => Promise<boolean> }) | null>(null);
export function useNotifications() { const value = useContext(NotificationContext); if (!value) throw new Error('NotificationProvider required'); return value; }
