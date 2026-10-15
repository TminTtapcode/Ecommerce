import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { userApi } from '../api/userApi';
import type { Profile, ProfileUpdate } from '../api/userApi';
import { decodeUser } from './authSession';
import type { UserPayload } from './authSession';
import { websocketService } from '../services/websocket';

export type { UserPayload } from './authSession';

interface AuthContextType {
  user: UserPayload | null;
  loading: boolean;
  isAuthenticated: boolean;
  isAdmin: boolean;
  isVendor: boolean;
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
  const [loading] = useState<boolean>(false);

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
      console.error(error);
      return null;
    }
  }, []);

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
      console.error(error);
      throw error;
    }
  }, []);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      websocketService.connect(token);
      void loadProfile();
    }
  }, [loadProfile]);

  useEffect(() => {
    const handleStorageChange = (event: StorageEvent) => {
      if (event.key === 'token') {
        const token = localStorage.getItem('token');
        const identity = decodeUser(token);
        setUser(identity);
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

  const login = (token: string) => {
    localStorage.setItem('token', token);
    const identity = decodeUser(token);
    setUser(identity);
    websocketService.connect(token);
    void loadProfile();
  };

  const logout = () => {
    localStorage.removeItem('token');
    setUser(null);
    websocketService.disconnect();
  };

  const isAuthenticated = user !== null;
  const isAdmin = user?.roles?.includes('ROLE_ADMIN') ?? false;
  const isVendor = user?.roles?.some((r) => r === 'ROLE_VENDOR' || r === 'ROLE_ADMIN') ?? false;

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        isAuthenticated,
        isAdmin,
        isVendor,
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
