import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { userApi } from '../api/userApi';
import type { Profile, ProfileUpdate } from '../api/userApi';
import { decodeUser } from './authSession';
import type { UserPayload } from './authSession';
import { websocketService } from '../services/websocket';
import type { RealtimeSession } from '../services/websocket';

export type { UserPayload } from './authSession';

interface AuthContextType {
  session: RealtimeSession | null;
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
  const [user, setUser] = useState<UserPayload | null>(() => {
    const token = localStorage.getItem('token');
    return decodeUser(token);
  });
  const [session, setSession] = useState<RealtimeSession | null>(() => {
    const token = localStorage.getItem('token');
    return token ? { token } : null;
  });
  const [loading] = useState<boolean>(false);

  // Hàm tải thông tin hồ sơ người dùng từ Backend
  const loadProfile = useCallback(async (): Promise<Profile | null> => {
    const token = localStorage.getItem('token');
    if (!token) return null;
    try {
      const response = await userApi.getProfile();
      const profile = response.data.data;
      if (profile) {
        setUser((prev) => (prev ? { ...prev, fullName: profile.fullName } : prev));
        return profile;
      }
      return null;
    } catch (error) {
      console.error('Lỗi khi tải thông tin cá nhân:', error);
      return null;
    }
  }, []);

  // Hàm cập nhật hồ sơ cá nhân
  const saveProfile = useCallback(async (body: ProfileUpdate): Promise<Profile | null> => {
    try {
      const response = await userApi.updateProfile(body);
      const profile = response.data.data;
      if (profile) {
        setUser((prev) => (prev ? { ...prev, fullName: profile.fullName } : prev));
        return profile;
      }
      return null;
    } catch (error) {
      console.error('Lỗi khi cập nhật hồ sơ:', error);
      throw error;
    }
  }, []);

  // Khi mở ứng dụng: nếu có token thì kết nối WebSocket và nạp hồ sơ
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      websocketService.connect(token);
      void loadProfile();
    }
  }, [loadProfile]);

  // Đồng bộ đa tab: khi đăng nhập/đăng xuất ở tab khác thì tự cập nhật giao diện
  useEffect(() => {
    const handleStorageChange = (event: StorageEvent) => {
      if (event.key === 'token') {
        const token = localStorage.getItem('token');
        const identity = decodeUser(token);
        setUser(identity);
        setSession(token ? { token } : null);
        if (token) {
          websocketService.connect(token);
          void loadProfile();
        } else {
          websocketService.disconnect();
        }
      }
    };
    window.addEventListener('storage', handleStorageChange);
    return () => window.removeEventListener('storage', handleStorageChange);
  }, [loadProfile]);

  // Hàm đăng nhập: Lưu token, set user, kết nối WebSocket và nạp profile
  const login = (token: string) => {
    localStorage.setItem('token', token);
    const identity = decodeUser(token);
    setUser(identity);
    setSession({ token });
    websocketService.connect(token);
    void loadProfile();
  };

  // Hàm đăng xuất: Xóa token, reset user và ngắt kết nối WebSocket
  const logout = () => {
    localStorage.removeItem('token');
    setUser(null);
    setSession(null);
    websocketService.disconnect();
  };

  const isAuthenticated = user !== null;
  const isAdmin = user?.roles?.includes('ROLE_ADMIN') ?? false;
  const isVendor = user?.roles?.some((r) => r === 'ROLE_VENDOR' || r === 'ROLE_ADMIN') ?? false;

  return (
    <AuthContext.Provider
      value={{
        session,
        isAuthenticated,
        isAdmin,
        isVendor,
        user,
        loading,
        login,
        logout,
        loadProfile,
        saveProfile,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
