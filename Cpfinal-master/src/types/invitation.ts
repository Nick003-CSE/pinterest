export type InvitationType = 'Board Collaboration' | 'Connection';

export interface Invitation {
  id: string;
  inviterId?: string;
  inviterName: string;
  inviterAvatar: string;
  boardId?: string;
  boardTitle?: string;
  type: InvitationType;
  message: string;
  sentAt: string; // ISO string for when it was sent
  status: 'pending' | 'accepted' | 'declined';
}

