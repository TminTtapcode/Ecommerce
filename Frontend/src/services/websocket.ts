import { Client } from '@stomp/stompjs';
import type { ChatMessage, SendMessageRequest } from '../api/types/chat.types';
import type { NotificationResponse } from '../api/types/notification.types';
import { authSessionGuard } from '../contexts/authSession';
export interface RealtimeSession { token: string; generation: number }
export function tokenExpiry(token: string): number {
  try { const exp = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))).exp;
    return typeof exp === 'number' ? exp * 1000 : 0;
  } catch { return 0; }
}
class WebSocketService {
  private client: Client | null = null;
  private sequence = 0;
  private closing: Promise<void> = Promise.resolve();
  private expiryTimer: ReturnType<typeof setTimeout> | undefined;
  private messages = new Set<(m: ChatMessage) => void>();
  private notifications = new Set<(n: NotificationResponse) => void>();
  private statuses = new Set<(connected: boolean) => void>();
  private connected = false;
  setSession(session: RealtimeSession | null) {
    const sequence = ++this.sequence;
    const old = this.client; this.client = null;
    clearTimeout(this.expiryTimer); this.status(false);
    this.closing = this.closing.then(async () => { if (old) await old.deactivate({ force: true }); });
    void this.closing.then(() => {
      if (!session || sequence !== this.sequence) return;
      const current = () => sequence === this.sequence && authSessionGuard.accepts(session.generation)
        && localStorage.getItem('token') === session.token && tokenExpiry(session.token) > Date.now();
      if (!current()) return;
      const api = import.meta.env.VITE_API_URL || 'http://localhost:8080';
      const url = import.meta.env.VITE_WS_URL || `${api.replace(/^http/, 'ws')}/ws`;
      const client = new Client({ brokerURL: url, connectHeaders: { Authorization: `Bearer ${session.token}` },
        reconnectDelay: 5000, heartbeatIncoming: 4000, heartbeatOutgoing: 4000,
        beforeConnect: async () => { if (!current()) await client.deactivate({ force: true }); },
        onConnect: () => {
          if (!current()) { void client.deactivate({ force: true }); return; }
          client.subscribe('/user/queue/messages', frame => {
            if (!current()) return;
            try { const value = JSON.parse(frame.body); this.messages.forEach(cb => cb(value)); } catch {  }
          });
          client.subscribe('/user/queue/notifications', frame => {
            if (!current()) return;
            try { const value = JSON.parse(frame.body); if (Number.isSafeInteger(value.id)) this.notifications.forEach(cb => cb(value)); } catch {  }
          });
          this.status(true);
        },
        onStompError: () => { if (sequence === this.sequence) this.status(false); void client.deactivate({ force: true }); },
        onWebSocketClose: () => { if (sequence === this.sequence) this.status(false); },
      });
      this.client = client;
      this.expiryTimer = setTimeout(() => { if (sequence === this.sequence) this.setSession(null); }, Math.min(tokenExpiry(session.token) - Date.now(), 2147483647));
      client.activate();
    });
  }
  subscribe(cb: (m: ChatMessage) => void) { this.messages.add(cb); return () => { this.messages.delete(cb); }; }
  subscribeNotifications(cb: (n: NotificationResponse) => void) { this.notifications.add(cb); return () => { this.notifications.delete(cb); }; }
  onStatusChange(cb: (connected: boolean) => void) { this.statuses.add(cb); cb(this.connected); return () => { this.statuses.delete(cb); }; }
  private status(value: boolean) { this.connected = value; this.statuses.forEach(cb => cb(value)); }
  isConnected() { return this.connected; }
  sendMessage(body: SendMessageRequest) {
    if (!this.client?.connected || !this.connected) return false;
    this.client.publish({ destination: '/app/chat.send', body: JSON.stringify(body) }); return true;
  }
}
export const websocketService = new WebSocketService();
