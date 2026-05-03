import { Pin } from '../types/pin';

export const sponsoredPins: Pin[] = [
  {
    id: 's-pin-1',
    title: 'Lumen Glow Table Lamp',
    description: 'Matte alabaster finish with dimmable brass crown.',
    category: 'Sponsored',
    imageUrl:
      'https://picsum.photos/id/1022/600/900',
    author: {
      name: 'Northlight Studio',
      location: 'Seattle, USA',
    },
    stats: {
      saves: 1100,
      shares: 89,
      likes: 0,
    },
    sponsored: true,
    sponsorName: 'Northlight Studio',
    sponsorWebsite: 'https://northlight-studio.example.com',
    createdAt: '2025-01-20T10:00:00Z',
    attribution: 'Sponsored content from Northlight Studio',
  },
  {
    id: 's-pin-2',
    title: 'Dune Modular Sofa',
    description: 'Low-slung seating with recycled boucle upholstery.',
    category: 'Sponsored',
    imageUrl:
      'https://picsum.photos/id/1019/600/900',
    author: {
      name: 'Atelier Forma',
      location: 'Los Angeles, USA',
    },
    stats: {
      saves: 2000,
      shares: 145,
      likes: 0,
    },
    sponsored: true,
    sponsorName: 'Atelier Forma',
    sponsorWebsite: 'https://atelier-forma.example.com',
    createdAt: '2025-01-18T14:30:00Z',
    attribution: 'Sponsored content from Atelier Forma',
  },
];

