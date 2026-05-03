import React, { createContext, useContext, useState, useEffect, useMemo } from 'react';
import { UserSummary } from '../types/user';
import { useInvitations } from './InvitationContext';

interface Collaboration {
  userId: string;
  userName: string;
  userAvatar: string;
  boardTitle: string;
  boardId?: string;
  invitedAt: string;
}

interface CollaborationContextValue {
  collaborations: Collaboration[];
  getCollaborators: () => UserSummary[];
  getCollaborationsByUser: (userId: string) => Collaboration[];
}

const CollaborationContext = createContext<CollaborationContextValue | undefined>(undefined);

const STORAGE_KEY = 'pinterest.collaborations';

const loadCollaborationsFromStorage = (): Collaboration[] => {
  try {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored) {
      return JSON.parse(stored);
    }
  } catch (error) {
    console.error('Error loading collaborations from storage:', error);
  }
  return [];
};

export const CollaborationProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [collaborations, setCollaborations] = useState<Collaboration[]>(loadCollaborationsFromStorage);
  const { invitations } = useInvitations();

  // Sync collaborations with accepted board collaboration invitations
  useEffect(() => {
    const acceptedBoardInvitations = invitations.filter(
      (inv) => inv.type === 'Board Collaboration' && inv.status === 'accepted'
    );

    const newCollaborations: Collaboration[] = acceptedBoardInvitations.map((inv) => ({
      userId: inv.inviterId || 'unknown',
      userName: inv.inviterName,
      userAvatar: inv.inviterAvatar,
      boardTitle: inv.boardTitle || 'Untitled Board',
      boardId: inv.boardId,
      invitedAt: inv.sentAt,
    }));

    // Merge with existing collaborations, avoiding duplicates
    setCollaborations((prev) => {
      const existing = new Map(
        prev.map((collab) => [`${collab.userId}-${collab.boardTitle}`, collab])
      );

      newCollaborations.forEach((collab) => {
        const key = `${collab.userId}-${collab.boardTitle}`;
        if (!existing.has(key)) {
          existing.set(key, collab);
        }
      });

      return Array.from(existing.values());
    });
  }, [invitations]);

  // Save to localStorage whenever collaborations change
  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(collaborations));
  }, [collaborations]);

  const getCollaborators = (): UserSummary[] => {
    const uniqueCollaborators = new Map<string, UserSummary>();

    collaborations.forEach((collab) => {
      if (!uniqueCollaborators.has(collab.userId)) {
        uniqueCollaborators.set(collab.userId, {
          id: collab.userId,
          name: collab.userName,
          username: `@${collab.userName.toLowerCase().replace(/\s+/g, '')}`,
          avatarUrl: collab.userAvatar,
        });
      }
    });

    return Array.from(uniqueCollaborators.values());
  };

  const getCollaborationsByUser = (userId: string): Collaboration[] => {
    return collaborations.filter((collab) => collab.userId === userId);
  };

  const value = useMemo<CollaborationContextValue>(
    () => ({
      collaborations,
      getCollaborators,
      getCollaborationsByUser,
    }),
    [collaborations]
  );

  return (
    <CollaborationContext.Provider value={value}>
      {children}
    </CollaborationContext.Provider>
  );
};

export const useCollaborations = () => {
  const context = useContext(CollaborationContext);
  if (!context) {
    throw new Error('useCollaborations must be used within a CollaborationProvider');
  }
  return context;
};

