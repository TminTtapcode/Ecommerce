import { Client } from '@stomp/stompjs';
import type { ChatMessage, SendMessageRequest } from '../api/types/chat.types';
import type { NotificationResponse } from '../api/types/notification.types';

class WebSocketService {
  private client: Client | null = null;
  private messages = new Set<(m: ChatMessage) => void>();
  private notifications = new Set<(n: NotificationResponse) => void>();
  private statuses = new Set<(connected: boolean) => void>();
  private connected = false;

  // 1. Kết nối tới WebSocket Server với Bearer Token
  connect(token: string) {
    if (this.client) {
      this.disconnect();
    }

    const api = import.meta.env.VITE_API_URL || 'http://localhost:8080';
    const url = import.meta.env.VITE_WS_URL || `${api.replace(/^http/, 'ws')}/ws`;

    const client = new Client({
      brokerURL: url,
      connectHeaders: {
        Authorization: `Bearer ${token}`, // Gửi token trong frame CONNECT của STOMP
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        this.status(true);

        // Kênh nhận tin nhắn chat riêng tư: /user/queue/messages
        client.subscribe('/user/queue/messages', (frame) => {
          try {
            const data = JSON.parse(frame.body);
            this.messages.forEach((cb) => cb(data));
          } catch (e) {
            console.error('Error parsing chat message:', e);
          }
        });

        // Kênh nhận thông báo hệ thống: /user/queue/notifications
        client.subscribe('/user/queue/notifications', (frame) => {
          try {
            const data = JSON.parse(frame.body);
            this.notifications.forEach((cb) => cb(data));
          } catch (e) {
            console.error('Error parsing notification:', e);
          }
        });
      },
      onStompError: () => {
        this.status(false);
      },
      onWebSocketClose: () => {
        this.status(false);
      },
    });

    this.client = client;
    client.activate();
  }

  // 2. Ngắt kết nối khi đăng xuất
  disconnect() {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
    this.status(false);
  }

  // 3. Đăng ký nhận tin nhắn chat
  subscribe(cb: (m: ChatMessage) => void) {
    this.messages.add(cb);
    return () => {
      this.messages.delete(cb);
    };
  }

  // 4. Đăng ký nhận thông báo
  subscribeNotifications(cb: (n: NotificationResponse) => void) {
    this.notifications.add(cb);
    return () => {
      this.notifications.delete(cb);
    };
  }

  // 5. Theo dõi trạng thái kết nối
  onStatusChange(cb: (connected: boolean) => void) {
    this.statuses.add(cb);
    cb(this.connected);
    return () => {
      this.statuses.delete(cb);
    };
  }

  private status(value: boolean) {
    this.connected = value;
    this.statuses.forEach((cb) => cb(value));
  }

  isConnected() {
    return this.connected;
  }

  // 6. Gửi tin nhắn chat lên server qua kênh /app/chat.send
  sendMessage(body: SendMessageRequest): boolean {
    if (!this.client?.connected || !this.connected) return false;
    this.client.publish({
      destination: '/app/chat.send',
      body: JSON.stringify(body),
    });
    return true;
  }
}

export const websocketService = new WebSocketService();
