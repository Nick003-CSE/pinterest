import React, { createContext, useContext, useMemo, useState } from 'react';
import axios from 'axios';
import { API_BASE_URL } from '../config/api';

interface StoredUser {
  email: string;
  password: string;
  phoneNumber?: string;
  userId?: number;
  username?: string;
  fullName?: string;
  avatarUrl?: string;
}

interface AuthContextValue {
  isAuthenticated: boolean;
  currentUser: StoredUser | null;
  signIn: (email: string, password: string) => Promise<{ ok: boolean; message: string }>;
  signOut: () => void;
  backendUserId: number | null;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

const STORAGE_KEYS = {
  auth: 'pinterest.auth',
  user: 'pinterest.user',
};

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(() => {
    return localStorage.getItem(STORAGE_KEYS.auth) === 'true';
  });
  const [currentUser, setCurrentUser] = useState<StoredUser | null>(() => {
    try {
      const raw = localStorage.getItem(STORAGE_KEYS.user);
      if (!raw) return null;
      return JSON.parse(raw) as StoredUser;
    } catch {
      return null;
    }
  });

  const signIn = async (email: string, password: string) => {
    try {
      const response = await axios.post<{
        message?: string;
        phoneNumber?: string;
        userId?: number;
        username?: string;
        fullName?: string;
        avatarUrl?: string;
      }>(
      `${API_BASE_URL}/auth/login`,
        {
          email,
          password,
        }
      );

      setIsAuthenticated(true);
      localStorage.setItem(STORAGE_KEYS.auth, 'true');
      const storedUser: StoredUser = {
        email,
        password,
        phoneNumber: response.data.phoneNumber,
        userId: response.data.userId,
        username: response.data.username,
        fullName: response.data.fullName,
        avatarUrl: response.data.avatarUrl,
      };
      localStorage.setItem(STORAGE_KEYS.user, JSON.stringify(storedUser));
      setCurrentUser(storedUser);

      return {
        ok: true,
        message: response.data.message ?? 'Login successful! Redirecting…',
      };
    } catch (error) {
      if (axios.isAxiosError(error) && error.response?.data?.message) {
        return { ok: false, message: error.response.data.message };
      }
      return { ok: false, message: 'Unable to sign in. Please try again.' };
    }
  };

  const signOut = () => {
    setIsAuthenticated(false);
    setCurrentUser(null);
    localStorage.removeItem(STORAGE_KEYS.auth);
    localStorage.removeItem(STORAGE_KEYS.user);
  };

  const value = useMemo<AuthContextValue>(
    () => ({
      isAuthenticated,
      currentUser,
      backendUserId: currentUser?.userId ?? null,
      signIn,
      signOut,
    }),
    [isAuthenticated, currentUser]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const persistUser = (user: StoredUser) => {
  localStorage.setItem(STORAGE_KEYS.user, JSON.stringify(user));
};

export const getUserByPhone = (phoneNumber: string): StoredUser | null => {
  try {
    const raw = localStorage.getItem(STORAGE_KEYS.user);
    if (!raw) return null;
    const parsed = JSON.parse(raw);
    if (parsed?.phoneNumber === phoneNumber) {
      return parsed;
    }
    return null;
  } catch {
    return null;
  }
};

export const resetPassword = (phoneNumber: string, newPassword: string): boolean => {
  try {
    const raw = localStorage.getItem(STORAGE_KEYS.user);
    if (!raw) return false;
    const parsed = JSON.parse(raw);
    if (parsed?.phoneNumber === phoneNumber) {
      const updatedUser = { ...parsed, password: newPassword };
      localStorage.setItem(STORAGE_KEYS.user, JSON.stringify(updatedUser));
      return true;
    }
    return false;
  } catch {
    return false;
  }
};

