export interface Collaborator {
  id: string;
  username: string;
  fullName: string;
  avatarUrl: string;
}

export interface Board {
  id: string;
  title: string;
  description: string;
  category: string;
  coverUrl: string;
  pinIds: string[];
  pinsPreview: import('./pin').Pin[];
  updatedAt: string;
  createdAt: string;
  isFeatured?: boolean;
  collaborators?: Collaborator[];
}

