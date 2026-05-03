import React, { createContext, useContext, useState, useEffect, useMemo, useCallback } from 'react';
import axios from 'axios';
import { Invitation } from '../types/invitation';
import { useAuth } from './AuthContext';
import { API_BASE_URL } from '../config/api';

interface InvitationContextValue {
  invitations: Invitation[];
  acceptInvitation: (invitationId: string) => Promise<{ success: boolean; error?: string }>;
  declineInvitation: (invitationId: string) => Promise<{ success: boolean; error?: string }>;
  ignoreInvitation: (invitationId: string) => Promise<{ success: boolean; error?: string }>;
  getPendingInvitations: () => Invitation[];
  refreshInvitations: () => Promise<void>;
  loading: boolean;
}

const InvitationContext = createContext<InvitationContextValue | undefined>(undefined);

const mapToInvitation = (data: any): Invitation => {
  const sentAtIso: string = data.sentAt
    ? new Date(data.sentAt).toISOString()
    : new Date().toISOString();

  return {
    id: String(data.id),
    inviterId: data.inviterId ? String(data.inviterId) : undefined,
    inviterName: data.inviterName || 'Unknown User',
    inviterAvatar:
      data.inviterAvatar ||
      `https://ui-avatars.com/api/?background=0f172a&color=fff&name=${encodeURIComponent(
        data.inviterName || 'User'
      )}`,
    boardId: data.boardId ? String(data.boardId) : undefined,
    boardTitle: data.boardTitle || undefined,
    type: data.type === 'Board Collaboration' ? 'Board Collaboration' : 'Connection',
    message: data.message || '',
    sentAt: sentAtIso,
    status:
      data.status?.toLowerCase() === 'pending'
        ? 'pending'
        : data.status?.toLowerCase() === 'accepted'
        ? 'accepted'
        : 'declined',
  };
};

const getTimeAgo = (date: Date): string => {
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffMins = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMs / 3600000);
  const diffDays = Math.floor(diffMs / 86400000);

  if (diffMins < 1) return 'just now';
  if (diffMins < 60) return `${diffMins}m ago`;
  if (diffHours < 24) return `${diffHours}h ago`;
  if (diffDays < 7) return `${diffDays}d ago`;
  return date.toLocaleDateString();
};

export const InvitationProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { backendUserId } = useAuth();
  const [invitations, setInvitations] = useState<Invitation[]>([]);
  const [loading, setLoading] = useState(false);

  const refreshInvitations = useCallback(async () => {
    if (!backendUserId) {
      setInvitations([]);
      return;
    }
    try {
      setLoading(true);
      // Load all invitations (pending + accepted + declined)
      const response = await axios.get(
        `${API_BASE_URL}/invitations/${backendUserId}/all`
      );
      const list = Array.isArray(response.data) ? response.data : [];
      setInvitations(list.map(mapToInvitation));
    } catch (error) {
      console.error('Failed to load invitations:', error);
      setInvitations([]);
    } finally {
      setLoading(false);
    }
  }, [backendUserId]);

  useEffect(() => {
    refreshInvitations();
  }, [refreshInvitations]);

  const acceptInvitation = async (invitationId: string): Promise<{ success: boolean; error?: string }> => {
    if (!backendUserId) {
      return { success: false, error: 'Not authenticated' };
    }
    try {
      const response = await axios.post(
        `${API_BASE_URL}/invitations/${backendUserId}/accept`,
        { invitationId: Number(invitationId) }
      );
      // Refresh invitations after accepting
      await refreshInvitations();
      return { success: true };
    } catch (error: any) {
      console.error('Failed to accept invitation:', error);
      const errorMsg = error.response?.data?.message || error.message || 'Failed to accept invitation';
      return { success: false, error: errorMsg };
    }
  };

  const declineInvitation = async (invitationId: string): Promise<{ success: boolean; error?: string }> => {
    if (!backendUserId) {
      return { success: false, error: 'Not authenticated' };
    }
    try {
      const response = await axios.post(
        `${API_BASE_URL}/invitations/${backendUserId}/decline`,
        { invitationId: Number(invitationId) }
      );
      // Refresh invitations after declining
      await refreshInvitations();
      return { success: true };
    } catch (error: any) {
      console.error('Failed to decline invitation:', error);
      const errorMsg = error.response?.data?.message || error.message || 'Failed to decline invitation';
      return { success: false, error: errorMsg };
    }
  };

  const ignoreInvitation = async (invitationId: string): Promise<{ success: boolean; error?: string }> => {
    // Ignore is similar to decline but doesn't notify the sender
    return declineInvitation(invitationId);
  };

  const getPendingInvitations = (): Invitation[] => {
    return invitations.filter((inv) => inv.status === 'pending');
  };

  const value = useMemo<InvitationContextValue>(
    () => ({
      invitations,
      acceptInvitation,
      declineInvitation,
      ignoreInvitation,
      getPendingInvitations,
      refreshInvitations,
      loading,
    }),
    [invitations, loading, refreshInvitations]
  );

  return (
    <InvitationContext.Provider value={value}>
      {children}
    </InvitationContext.Provider>
  );
};

export const useInvitations = () => {
  const context = useContext(InvitationContext);
  if (!context) {
    throw new Error('useInvitations must be used within an InvitationProvider');
  }
  return context;
};

