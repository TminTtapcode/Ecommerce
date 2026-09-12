import { useEffect, type ReactNode } from 'react';
import { useAuth } from './AuthContext';
import { websocketService } from '../services/websocket';
export function RealtimeProvider({ children }: { children: ReactNode }) {
  const { session } = useAuth();
  useEffect(() => { websocketService.setSession(session); return () => websocketService.setSession(null); }, [session]);
  return children;
}
