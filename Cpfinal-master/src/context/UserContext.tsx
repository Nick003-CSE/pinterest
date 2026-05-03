import React, { createContext, useContext, useState, useEffect, useMemo, useCallback } from 'react';
import axios from 'axios';
import { UserSummary } from '../types/user';
import { currentUser } from '../data/user';
import { API_BASE_URL } from '../config/api';
import { useAuth } from './AuthContext';

interface UserContextValue {
  followers: UserSummary[];
  following: UserSummary[];
  addToFollowing: (user: UserSummary) => Promise<void>;
  removeFromFollowing: (userId: string) => Promise<void>;
  isFollowing: (userId: string) => boolean;
  refreshFollowers: () => Promise<void>;
  refreshFollowing: () => Promise<void>;
  getFollowersForUser: (userId: string) => Promise<UserSummary[]>;
  getFollowingForUser: (userId: string) => Promise<UserSummary[]>;
  loading: boolean;
}

const UserContext = createContext<UserContextValue | undefined>(undefined);

const DEFAULT_AVATAR = (name: string) =>
  `https://ui-avatars.com/api/?background=0f172a&color=fff&name=${encodeURIComponent(name)}`;

const mapToSummary = (user: {
  userId?: number;
  username?: string;
  fullName?: string;
  name?: string;
  id?: string;
  avatarUrl?: string;
}): UserSummary => {
  const name = user.fullName || user.name || user.username || 'Framescape user';
  const username = user.username ? (user.username.startsWith('@') ? user.username : `@${user.username}`) : '@pinner';
  return {
    id: user.userId ? String(user.userId) : user.id || String(Date.now()),
    name,
    username,
    avatarUrl: user.avatarUrl || DEFAULT_AVATAR(name),
  };
};

export const UserProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { backendUserId } = useAuth();
  const [followers, setFollowers] = useState<UserSummary[]>(currentUser.followers);
  const [following, setFollowing] = useState<UserSummary[]>(currentUser.following);
  const [loading, setLoading] = useState(false);

  const refreshFollowers = useCallback(async () => {
    if (!backendUserId) {
      setFollowers(currentUser.followers);
      return;
    }
    try {
      const response = await axios.get(
        `${API_BASE_URL}/followers/${backendUserId}/followers`
      );
      const list = Array.isArray(response.data) ? response.data : [];
      console.log('Followers raw data:', list);
      const mapped = list.map(mapToSummary);
      console.log('Followers mapped:', mapped);
      setFollowers(mapped);
    } catch (error) {
      console.error('Failed to load followers:', error);
      if (axios.isAxiosError(error)) {
        console.error('Followers error response:', error.response?.data, error.response?.status);
      }
      setFollowers([]);
    }
  }, [backendUserId]);

  const refreshFollowing = useCallback(async () => {
    if (!backendUserId) {
      setFollowing(currentUser.following);
      return;
    }
    try {
      const response = await axios.get(
        `${API_BASE_URL}/followers/${backendUserId}/following`
      );
      const list = Array.isArray(response.data) ? response.data : [];
      console.log('Following raw data:', list);
      const mapped = list.map(mapToSummary);
      console.log('Following mapped:', mapped);
      setFollowing(mapped);
    } catch (error) {
      console.error('Failed to load following:', error);
      if (axios.isAxiosError(error)) {
        console.error('Following error response:', error.response?.data, error.response?.status);
      }
      setFollowing([]);
    }
  }, [backendUserId]);

  useEffect(() => {
    let ignore = false;
    const init = async () => {
      console.log('=== UserContext useEffect ===');
      console.log('backendUserId:', backendUserId);
      if (!backendUserId) {
        console.log('No backendUserId, using fallback data');
        setFollowers(currentUser.followers);
        setFollowing(currentUser.following);
        setLoading(false);
        return;
      }
      console.log('Loading followers/following for userId:', backendUserId);
      setLoading(true);
      try {
        await Promise.all([refreshFollowers(), refreshFollowing()]);
        console.log('Both API calls completed');
      } catch (error) {
        console.error('Failed to initialize followers/following:', error);
      }
      if (!ignore) {
        setLoading(false);
      }
    };
    init();
    return () => {
      ignore = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [backendUserId]);

  const addToFollowing = async (user: UserSummary) => {
    if (!user?.id) return;
    setFollowing((prev) => {
      if (prev.some((u) => u.id === user.id)) {
        return prev;
      }
      return [...prev, user];
    });
    if (!backendUserId) return;
    try {
      await axios.post(`${API_BASE_URL}/followers`, {
        followerId: backendUserId,
        followingId: Number(user.id),
      });
      await refreshFollowing();
    } catch (error) {
      console.error('Failed to follow user:', error);
    }
  };

  const removeFromFollowing = async (userId: string) => {
    setFollowing((prev) => prev.filter((user) => user.id !== userId));
    if (!backendUserId) return;
    try {
      await axios.delete(`${API_BASE_URL}/followers`, {
        data: {
          followerId: backendUserId,
          followingId: Number(userId),
        },
      });
      await refreshFollowing();
    } catch (error) {
      console.error('Failed to unfollow user:', error);
    }
  };

  const isFollowing = (userId: string): boolean => {
    return following.some((user) => user.id === userId);
  };

  const getFollowersForUser = async (userId: string): Promise<UserSummary[]> => {
    try {
      const response = await axios.get(
        `${API_BASE_URL}/followers/${userId}/followers`
      );
      const list = Array.isArray(response.data) ? response.data : [];
      return list.map(mapToSummary);
    } catch (error) {
      console.error('Failed to load followers for user:', error);
      return [];
    }
  };

  const getFollowingForUser = async (userId: string): Promise<UserSummary[]> => {
    try {
      const response = await axios.get(
        `${API_BASE_URL}/followers/${userId}/following`
      );
      const list = Array.isArray(response.data) ? response.data : [];
      return list.map(mapToSummary);
    } catch (error) {
      console.error('Failed to load following for user:', error);
      return [];
    }
  };

  const value = useMemo<UserContextValue>(
    () => ({
      followers,
      following,
      addToFollowing,
      removeFromFollowing,
      isFollowing,
      refreshFollowers,
      refreshFollowing,
      getFollowersForUser,
      getFollowingForUser,
      loading,
    }),
    [followers, following, loading, refreshFollowers, refreshFollowing]
  );

  return (
    <UserContext.Provider value={value}>
      {children}
    </UserContext.Provider>
  );
};

export const useUser = () => {
  const context = useContext(UserContext);
  if (!context) {
    throw new Error('useUser must be used within a UserProvider');
  }
  return context;
};
