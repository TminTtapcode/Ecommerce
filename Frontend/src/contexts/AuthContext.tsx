import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { userApi } from '../api/userApi';
import type { Profile, ProfileUpdate } from '../api/userApi';
import { decodeUser, authSessionGuard } from './authSession';
import type { UserPayload } from './authSession';
export type { UserPayload } from './authSession';

interface AuthContextType {
  session: import("../services/websocket").RealtimeSession | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  isVendor: boolean;
  user: UserPayload | null;
  loading: boolean;
  login: (token: string) => void;
  logout: () => void;
  loadProfile: () => Promise<Profile | null>;
  saveProfile: (body: ProfileUpdate) => Promise<Profile | null>;
}
const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState(() => decodeUser(localStorage.getItem('token')));
  const [session, setSession] = useState(() => {
    const token = localStorage.getItem('token');
    return token && decodeUser(token) ? { token, generation: authSessionGuard.capture() } : null;
  });
  const requestVersion = React.useRef(0);
  const profileRequest = useCallback(async (body?: ProfileUpdate): Promise<Profile | null> => {
    const generation = authSessionGuard.capture();
    const token = localStorage.getItem('token');
    const identity = decodeUser(token);
    if (!identity || !token) return null;

    const version = body ? ++requestVersion.current : requestVersion.current;
    try {
      const session = { token, generation };
      const response = body ? await userApi.updateProfile(body, session) : await userApi.getProfile(session);
      if (!authSessionGuard.accepts(generation) || localStorage.getItem('token') !== token || version !== requestVersion.current) return null;
      const profile = response.data.data;
      if (!profile || profile.id !== identity.userId) throw new Error('Profile identity mismatch');
      setUser(current => current?.userId === identity.userId ? { ...current, fullName: profile.fullName } : current);
      return profile;
    } catch (error) {
      if (!authSessionGuard.accepts(generation) || localStorage.getItem('token') !== token || version !== requestVersion.current) return null;
      throw error;
    }
  }, []);
  const loadProfile = useCallback(() => profileRequest(), [profileRequest]);
  const saveProfile = useCallback((body: ProfileUpdate) => profileRequest(body), [profileRequest]);
  const invalidateRequests = useCallback(() => { ++requestVersion.current; }, []);
  useEffect(() => {
    void loadProfile().catch(() => {  });
    return invalidateRequests;
  }, [loadProfile, invalidateRequests]);
  useEffect(() => {
    const changed = (event: StorageEvent) => {
      if (event.storageArea !== localStorage || (event.key !== 'token' && event.key !== null)) return;
      authSessionGuard.advance();
      invalidateRequests();
      const token = localStorage.getItem('token');
      const identity = decodeUser(token);
      setUser(identity);
      setSession(token && identity ? { token, generation: authSessionGuard.capture() } : null);
      if (identity) void loadProfile().catch(() => {  });
    };
    window.addEventListener('storage', changed);
    return () => window.removeEventListener('storage', changed);
  }, [loadProfile, invalidateRequests]);
  const login = (token: string) => {
    authSessionGuard.advance();
    localStorage.setItem('token', token);
    setUser(decodeUser(token));
    setSession(decodeUser(token) ? { token, generation: authSessionGuard.capture() } : null);
    void loadProfile().catch(() => {  });
  };
  const logout = () => {
    authSessionGuard.advance();
    ++requestVersion.current;
    localStorage.removeItem('token');
    setUser(null);
    setSession(null);
  };
  return <AuthContext.Provider value={{ session, user, isAuthenticated: user !== null,
    isAdmin: user?.roles.includes('ROLE_ADMIN') ?? false,
    isVendor: user?.roles.some(role => role === 'ROLE_VENDOR' || role === 'ROLE_ADMIN') ?? false,
    loading: false, login, logout, loadProfile, saveProfile }}>{children}</AuthContext.Provider>;
};
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) throw new Error('useAuth must be used within an AuthProvider');
  return context;
};
