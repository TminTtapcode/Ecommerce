import { useEffect, useRef, useState, type ReactNode } from 'react';
import { useAuth } from './AuthContext';
import { authSessionGuard } from './authSession';
import { websocketService } from '../services/websocket';
import { notificationApi } from '../api/notificationApi';
import { NotificationContext as Context, type NotificationView as View } from '../hooks/useNotifications';
const empty: View = { notifications: [], unreadCount: 0, isLoading: false, error: '' };
type Actions = { refresh: () => void; read: (id?: number) => Promise<boolean> };
export function NotificationProvider({ children }: { children: ReactNode }) {
  const { session } = useAuth();
  return <NotificationSessionProvider key={session?.generation ?? 'anonymous'}>{children}</NotificationSessionProvider>;
}
function NotificationSessionProvider({ children }: { children: ReactNode }) {
  const { session } = useAuth();
  const [view, setView] = useState<View>(empty);
  const actions = useRef<Actions>({ refresh: () => {}, read: async () => false });
  useEffect(() => {
    setView(empty);
    if (!session) { actions.current = { refresh: () => {}, read: async () => false }; return; }
    let alive = true, running = false, dirty = false, revision = 0, writing = 0;
    const seen = new Set<number>();
    const valid = () => alive && authSessionGuard.accepts(session.generation) && localStorage.getItem('token') === session.token;
    const refresh = () => { if (!valid()) return; dirty = true; revision++; void drain(); };
    async function drain() {
      if (running || writing || !valid()) return;
      running = true; setView(v => ({ ...v, isLoading: true }));
      try {
        while (dirty && valid() && !writing) {
          dirty = false; const captured = revision;
          try {
            const [list, count] = await Promise.all([notificationApi.getNotifications(session!), notificationApi.getUnreadCount(session!)]);
            if (!valid()) return;
            if (captured !== revision || writing) { dirty = true; continue; }
            setView({ notifications: list.data.data?.content ?? [], unreadCount: count.data.data?.unreadCount ?? 0, isLoading: true, error: '' });
          } catch {
            if (valid()) setView(v => ({ ...v, error: 'Không tải được thông báo. Vui lòng thử lại.' }));
            if (captured === revision) dirty = false;
            break;
          }
        }
      } finally {
        running = false;
        if (valid()) {
          setView(v => ({ ...v, isLoading: false }));
          if (dirty && !writing) queueMicrotask(() => { void drain(); });
        }
      }
    }
    actions.current = { refresh, read: async id => {
      if (!valid()) return false;
      writing++; revision++; dirty = true;
      try {
        if (id === undefined) await notificationApi.markAllAsRead(session); else await notificationApi.markAsRead(session, id);
        return valid();
      } catch { if (valid()) setView(v => ({ ...v, error: 'Không đánh dấu đọc được. Vui lòng thử lại.' })); return false;
      } finally { writing--; if (valid()) refresh(); }
    } };
    const offPush = websocketService.subscribeNotifications(n => {
      if (seen.has(n.id)) return;
      seen.add(n.id); if (seen.size > 500) seen.delete(seen.values().next().value!);
      refresh();
    });
    const offStatus = websocketService.onStatusChange(connected => { if (connected) refresh(); });
    const interval = setInterval(() => { if (!websocketService.isConnected()) refresh(); }, 60000);
    const focus = () => { if (document.visibilityState === 'visible') refresh(); };
    window.addEventListener('focus', focus); document.addEventListener('visibilitychange', focus); refresh();
    return () => { alive = false; offPush(); offStatus(); clearInterval(interval); window.removeEventListener('focus', focus); document.removeEventListener('visibilitychange', focus); };
  }, [session]);
  return <Context.Provider value={{ ...view, refresh: () => actions.current.refresh(), read: id => actions.current.read(id) }}>{children}</Context.Provider>;
}
