import type { SessionRequestConfig } from './apiClient';
import type { RealtimeSession } from '../services/websocket';
import { authSessionGuard } from '../contexts/authSession';
export function sessionConfig(session: RealtimeSession): SessionRequestConfig {
  if (!authSessionGuard.accepts(session.generation) || localStorage.getItem('token') !== session.token) throw new Error('Stale session');
  return { headers: { Authorization: `Bearer ${session.token}` }, authSessionGeneration: session.generation };
}
