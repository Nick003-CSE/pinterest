export interface BusinessProfile {
  id: string;
  name: string;
  username: string;
  description: string;
  logoUrl: string;
  websiteUrl: string;
  category: string;
  verified: boolean;
  followerCount: number;
  isFollowing: boolean;
  createdAt: string;
}

export interface Showcase {
  id: string;
  businessProfileId: string;
  businessName: string;
  title: string;
  description: string;
  theme: string;
  coverImageUrl: string;
  featured: boolean;
  pinCount: number;
  pinIds: string[];
  createdAt: string;
}
