import { Invitation } from '../types/invitation';

export const invitations: Invitation[] = [
  {
    id: 'inv-1',
    inviterId: 'u-7',
    inviterName: 'Studio Mysa',
    inviterAvatar: 'https://picsum.photos/id/64/80/80',
    boardTitle: 'Quiet Workspace Moodboard',
    type: 'Board Collaboration',
    message: 'Help refine the palette and typography direction for our February drop.',
    sentAt: '5m ago',
    status: 'pending',
  },
  {
    id: 'inv-2',
    inviterId: 'u-8',
    inviterName: 'Willow & Clay',
    inviterAvatar: 'https://picsum.photos/id/65/80/80',
    type: 'Connection',
    message: 'We love your mindful hosting series—let\'s stay in touch!',
    sentAt: '1h ago',
    status: 'pending',
  },
  {
    id: 'inv-3',
    inviterId: 'u-9',
    inviterName: 'Minimalist Living',
    inviterAvatar: 'https://picsum.photos/id/66/80/80',
    boardTitle: 'Nordic Home Inspiration',
    type: 'Board Collaboration',
    message: 'Would love your input on our Scandinavian design board!',
    sentAt: '2h ago',
    status: 'pending',
  },
];

