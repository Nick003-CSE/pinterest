export interface PinAuthor {
  name: string;
  avatarUrl?: string;
  location?: string;
}

export interface PinStats {
  saves: number;
  shares: number;
  likes: number;
}

export interface Pin {
  id: string;
  ownerId?: string;
  ownerUsername?: string;
  title: string;
  description: string;
  category?: string;
  imageUrl?: string;
  author: PinAuthor;
  board?: string;
  stats: PinStats;
  palette?: string[];
  keywords?: string[];
  sponsored?: boolean;
  sponsorName?: string;
  sponsorWebsite?: string;
  createdAt: string;
  attribution?: string;
  // Backend status for distinguishing published pins from drafts
  status?: 'DRAFT' | 'PUBLISHED';
}

