import { boards } from './boards';
import { pins } from './pins';
import { UserProfile } from '../types/user';

export const currentUser: UserProfile = {
  id: 'user-1',
  name: 'Zyazk Sahu',
  username: '@zyazksahu',
  bio: 'Exploring design, inspiration boards, and mindful routines—one pin at a time.',
  avatarUrl: 'https://d2v5dzhdg4zhx3.cloudfront.net/web-assets/images/storypages/short/linkedin-profile-picture-maker/dummy_image/thumb/004.webp',
  location: 'Bangalore, India',
  website: 'https://pinterest.example.com/zyazksahu',
  savedPins: pins.slice(0, 6),
  boards,
  followers: [
    {
      id: 'u-2',
      name: 'Lena Aoki',
      username: '@lenastudio',
      avatarUrl: 'https://picsum.photos/id/64/120/120',
      role: 'Interior Artist',
    },
    {
      id: 'u-3',
      name: 'Marco Tan',
      username: '@marcosets',
      avatarUrl: 'https://picsum.photos/id/65/120/120',
      role: 'Photographer',
    },
  ],
  following: [
    {
      id: 'u-4',
      name: 'Samira K.',
      username: '@samiraclay',
      avatarUrl: 'https://picsum.photos/id/66/120/120',
      role: 'Ceramicist',
    },
    {
      id: 'u-5',
      name: 'Tanner Lee',
      username: '@slowbuilds',
      avatarUrl: 'https://picsum.photos/id/67/120/120',
      role: 'Architect',
    },
    {
      id: 'u-6',
      name: 'Studio Nova',
      username: '@studio.nova',
      avatarUrl: 'https://picsum.photos/id/68/120/120',
      role: 'Atelier',
    },
  ],
  stats: {
    pins: 128,
    followers: 4520,
    following: 320,
    boards: 12,
  },
};

