export interface SpotlightCollection {
  id: string;
  title: string;
  description: string;
  coverUrl: string;
  gradient: string;
}

export const spotlightCollections: SpotlightCollection[] = [
  {
    id: 'spot-1',
    title: 'Soothing Entryways',
    description: 'Layer stoneware, grounded lighting, and airy botanicals.',
    coverUrl:
      'https://picsum.photos/id/1023/600/400',
    gradient: 'linear-gradient(120deg, #1d976c, #93f9b9)',
  },
  {
    id: 'spot-2',
    title: 'Warm Neutrals',
    description: 'Boards for honeyed timbers and chalky plaster finishes.',
    coverUrl:
      'https://picsum.photos/id/1020/600/400',
    gradient: 'linear-gradient(135deg, #ff9a9e, #fad0c4)',
  },
  {
    id: 'spot-3',
    title: 'Analog Workflows',
    description: 'Mindfully designed desk setups for deep focus.',
    coverUrl:
      'https://picsum.photos/id/1012/600/400',
    gradient: 'linear-gradient(135deg, #a18cd1, #fbc2eb)',
  },
];

