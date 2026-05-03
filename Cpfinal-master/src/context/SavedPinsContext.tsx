import React, { createContext, useContext, useState, useEffect, useMemo } from 'react';
import axios from 'axios';
import { API_BASE_URL } from '../config/api';
import { Pin } from '../types/pin';
import { useAuth } from './AuthContext';

interface SavedPinsContextValue {
  savedPins: Pin[];
  savePin: (pin: Pin) => void;
  unsavePin: (pinId: string) => void;
  isPinSaved: (pinId: string) => boolean;
  refreshPins: () => Promise<void>;
}

const SavedPinsContext = createContext<SavedPinsContextValue | undefined>(undefined);

export const SavedPinsProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { currentUser } = useAuth();
  const [savedPins, setSavedPins] = useState<Pin[]>([]);

  const refreshPins = async () => {
    if (!currentUser?.userId) {
      setSavedPins([]);
      return;
    }
    try {
      const response = await axios.get(`${API_BASE_URL}/pins/owner/${currentUser.userId}`);
      const backendPins = response.data as any[];
      const mapped: Pin[] = backendPins.map((p) => ({
        id: String(p.id),
        ownerId: p.ownerId ? String(p.ownerId) : currentUser?.userId ? String(currentUser.userId) : undefined,
        ownerUsername: p.ownerUsername,
        title: p.title,
        description: p.description ?? '',
        category: p.boardName || 'Uncategorized',
        imageUrl: p.mediaUrl,
        author: {
          name: p.ownerFullName || p.ownerUsername || 'You',
        },
        board: p.boardName,
        stats: {
          saves: Number(p.saveCount ?? 0),
          shares: Number(p.shareCount ?? 0),
          likes: Number(p.likeCount ?? 0),
        },
        keywords: p.keywords ?? [],
        createdAt: p.createdAt ?? new Date().toISOString(),
        attribution: p.attribution,
        status: p.status,
      }));
      setSavedPins(mapped);
    } catch (error) {
      console.error('Failed to fetch saved pins from backend:', error);
      setSavedPins([]);
    }
  };

  useEffect(() => {
    refreshPins();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentUser?.userId]);

  const savePin = (pin: Pin) => {
    setSavedPins((prev) => {
      // Check if pin is already saved
      if (prev.some((p) => p.id === pin.id)) {
        return prev;
      }
      return [...prev, pin];
    });
  };

  const unsavePin = (pinId: string) => {
    setSavedPins((prev) => prev.filter((pin) => pin.id !== pinId));
  };

  const isPinSaved = (pinId: string): boolean => {
    return savedPins.some((pin) => pin.id === pinId);
  };

  const value = useMemo<SavedPinsContextValue>(
    () => ({
      savedPins,
      savePin,
      unsavePin,
      isPinSaved,
      refreshPins,
    }),
    [savedPins]
  );

  return <SavedPinsContext.Provider value={value}>{children}</SavedPinsContext.Provider>;
};

export const useSavedPins = () => {
  const context = useContext(SavedPinsContext);
  if (!context) {
    throw new Error('useSavedPins must be used within a SavedPinsProvider');
  }
  return context;
};

