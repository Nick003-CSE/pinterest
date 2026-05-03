import { pins } from './pins';
import { Board } from '../types/board';

const pickPins = (ids: string[]) => pins.filter((pin) => ids.includes(pin.id));

export const boards: Board[] = [
  {
    id: 'board-1',
    title: 'Mindful Morning Rituals',
    description: 'Warm textures, slow coffee moments, and quiet work nooks.',
    category: 'Wellness',
    coverUrl:
      'https://picsum.photos/id/1018/800/600',
    pinIds: ['pin-2', 'pin-8', 'pin-5', 'pin-1'],
    pinsPreview: pickPins(['pin-2', 'pin-8', 'pin-5']),
    updatedAt: 'Updated 2h ago',
    createdAt: '2024-12-01T10:00:00Z',
    isFeatured: true,
  },
  {
    id: 'board-2',
    title: 'Soft Minimal Interiors',
    description: 'Calming palettes, layered neutrals, and sculptural botanicals.',
    category: 'Home & Decor',
    coverUrl:
      'https://picsum.photos/id/1020/800/600',
    pinIds: ['pin-1', 'pin-3', 'pin-6'],
    pinsPreview: pickPins(['pin-1', 'pin-3', 'pin-6']),
    updatedAt: 'Updated yesterday',
    createdAt: '2024-11-20T08:00:00Z',
  },
  {
    id: 'board-3',
    title: 'Slow Travel Sketchbook',
    description: 'Intentional travel itineraries and analog journaling ideas.',
    category: 'Travel',
    coverUrl:
      'https://picsum.photos/id/1018/800/600',
    pinIds: ['pin-5', 'pin-7', 'pin-4'],
    pinsPreview: pickPins(['pin-5', 'pin-7', 'pin-4']),
    updatedAt: 'Updated 3 days ago',
    createdAt: '2024-10-15T18:00:00Z',
  },
];

