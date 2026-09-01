import React, { createContext, useContext, useState, useEffect } from 'react';

export interface UserPayload {
  userId: number;
  fullName?: string;
  sub: string;
  roles: string[];
}

interface AuthContextType {
  isAuthenticated: boolean;
  isAdmin: boolean;
  user: UserPayload | null;
  login: (token: string) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

// Helper function to decode JWT payload
const decodeToken = (token: string): any => {
  try {
    const payload = token.split('.')[1];
    return JSON.parse(atob(payload));
  } catch (e) {
    return null;
  }
};

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [isAdmin, setIsAdmin] = useState<boolean>(false);
  const [user, setUser] = useState<UserPayload | null>(null);

  const processToken = (token: string) => {
    setIsAuthenticated(true);
    const decoded = decodeToken(token);
    if (decoded) {
      if (decoded.roles && Array.isArray(decoded.roles)) {
        setIsAdmin(decoded.roles.includes('ROLE_ADMIN'));
      } else {
        setIsAdmin(false);
      }
      
      setUser({
        userId: decoded.userId || 0,
        fullName: decoded.fullName,
        sub: decoded.sub,
        roles: decoded.roles || [],
      });
    }
  };

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      processToken(token);
    }
  }, []);

  const login = (token: string) => {
    localStorage.setItem('token', token);
    processToken(token);
  };

  const logout = () => {
    localStorage.removeItem('token');
    setIsAuthenticated(false);
    setIsAdmin(false);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ isAuthenticated, isAdmin, user, login, logout }}>
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
