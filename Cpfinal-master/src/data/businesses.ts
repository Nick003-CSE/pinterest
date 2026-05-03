import { BusinessProfile } from '../types/business';

// Note: This is legacy mock data. Business profiles are now fetched from the backend API.
// This file is kept for backward compatibility but should not be used in new code.
export const businessProfiles: BusinessProfile[] = [
  {
    id: 'biz-1',
    name: 'Solstice Atelier',
    username: 'solstice-atelier',
    description: 'Sculpted lighting and ceramics for modern rituals.',
    websiteUrl: 'https://solstice-atelier.example.com',
    logoUrl:
      'https://picsum.photos/id/1021/800/800',
    category: 'Lighting & Decor',
    verified: true,
    followerCount: 0,
    isFollowing: false,
    createdAt: new Date().toISOString(),
  },
  {
    id: 'biz-2',
    name: 'Fieldstone Studio',
    username: 'fieldstone-studio',
    description: 'Textile-driven furniture and modular storage for calm homes.',
    websiteUrl: 'https://fieldstone.example.com',
    logoUrl:
      'https://picsum.photos/id/1019/800/800',
    category: 'Furniture & Storage',
    verified: true,
    followerCount: 0,
    isFollowing: false,
    createdAt: new Date().toISOString(),
  },
];

