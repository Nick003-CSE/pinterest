import { Board } from './board';
import { Pin } from './pin';

export interface UserStats {
  pins: number;
  followers: number;
  following: number;
  boards: number;
}

export interface UserProfile {
  id: string;
  name: string;
  username: string;
  bio: string;
  avatarUrl: string;
  location: string;
  website?: string;
  savedPins: Pin[];
  boards: Board[];
  followers: UserSummary[];
  following: UserSummary[];
  stats: UserStats;
}

export interface UserSummary {
  id: string;
  name: string;
  username: string;
  avatarUrl: string;
  role?: string;
}

