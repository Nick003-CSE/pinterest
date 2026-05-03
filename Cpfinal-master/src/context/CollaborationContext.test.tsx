import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { CollaborationProvider, useCollaborations } from './CollaborationContext';

// Mock the InvitationContext hook so we can control incoming invitations
jest.mock('./InvitationContext', () => ({
  useInvitations: jest.fn(),
}));

import { useInvitations } from './InvitationContext';

const mockedUseInvitations = useInvitations as jest.MockedFunction<typeof useInvitations>;

const STORAGE_KEY = 'pinterest.collaborations';

describe('CollaborationContext', () => {
  beforeEach(() => {
    mockedUseInvitations.mockReset();
    window.localStorage.clear();
    jest.spyOn(window.localStorage.__proto__, 'getItem');
    jest.spyOn(window.localStorage.__proto__, 'setItem');
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  const Wrapper: React.FC<{ children: React.ReactNode }> = ({ children }) => (
    <CollaborationProvider>{children}</CollaborationProvider>
  );

  const TestConsumer: React.FC = () => {
    const { collaborations, getCollaborators, getCollaborationsByUser } = useCollaborations();

    return (
      <div>
        <div data-testid="collab-count">{collaborations.length}</div>
        <div data-testid="collaborator-count">{getCollaborators().length}</div>
        <div data-testid="user-collab-count">
          {getCollaborationsByUser('1').length}
        </div>
      </div>
    );
  };

  it('loads collaborations from localStorage on initial render', () => {
    const stored = [
      {
        userId: '1',
        userName: 'Alice',
        userAvatar: 'avatar-1',
        boardTitle: 'Board A',
        boardId: '10',
        invitedAt: '2025-01-01T00:00:00.000Z',
      },
    ];

    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(stored));
    mockedUseInvitations.mockReturnValue({ invitations: [] } as any);

    render(
      <Wrapper>
        <TestConsumer />
      </Wrapper>
    );

    expect(screen.getByTestId('collab-count').textContent).toBe('1');
    expect(window.localStorage.getItem).toHaveBeenCalledWith(STORAGE_KEY);
  });

  it('syncs collaborations from accepted board invitations and avoids duplicates', async () => {
    mockedUseInvitations.mockReturnValue({
      invitations: [
        {
          id: '1',
          inviterId: '1',
          inviterName: 'Alice Wonderland',
          inviterAvatar: 'avatar-1',
          boardId: '10',
          boardTitle: 'Board A',
          type: 'Board Collaboration',
          status: 'accepted',
          message: '',
          sentAt: '2025-01-01T00:00:00.000Z',
        },
        // Duplicate same user + board should not create a second collaboration
        {
          id: '2',
          inviterId: '1',
          inviterName: 'Alice Wonderland',
          inviterAvatar: 'avatar-1',
          boardId: '10',
          boardTitle: 'Board A',
          type: 'Board Collaboration',
          status: 'accepted',
          message: '',
          sentAt: '2025-01-02T00:00:00.000Z',
        },
        // Different board from same user should create another collaboration
        {
          id: '3',
          inviterId: '1',
          inviterName: 'Alice Wonderland',
          inviterAvatar: 'avatar-1',
          boardId: '11',
          boardTitle: 'Board B',
          type: 'Board Collaboration',
          status: 'accepted',
          message: '',
          sentAt: '2025-01-03T00:00:00.000Z',
        },
        // Non accepted or non board-collaboration invitations should be ignored
        {
          id: '4',
          inviterId: '2',
          inviterName: 'Bob',
          inviterAvatar: 'avatar-2',
          boardId: '12',
          boardTitle: 'Board C',
          type: 'Board Collaboration',
          status: 'pending',
          message: '',
          sentAt: '2025-01-04T00:00:00.000Z',
        },
      ],
    } as any);

    render(
      <Wrapper>
        <TestConsumer />
      </Wrapper>
    );

    await waitFor(() => {
      expect(screen.getByTestId('collab-count').textContent).toBe('2');
    });

    // One collaborator (Alice) across both boards
    expect(screen.getByTestId('collaborator-count').textContent).toBe('1');
    // Collaborations by userId '1' should be 2
    expect(screen.getByTestId('user-collab-count').textContent).toBe('2');
  });

  it('persists collaborations to localStorage whenever they change', async () => {
    mockedUseInvitations.mockReturnValue({
      invitations: [
        {
          id: '1',
          inviterId: '1',
          inviterName: 'Alice',
          inviterAvatar: 'avatar-1',
          boardId: '10',
          boardTitle: 'Board A',
          type: 'Board Collaboration',
          status: 'accepted',
          message: '',
          sentAt: '2025-01-01T00:00:00.000Z',
        },
      ],
    } as any);

    render(
      <Wrapper>
        <TestConsumer />
      </Wrapper>
    );

    await waitFor(() => {
      expect(window.localStorage.setItem).toHaveBeenCalledWith(
        STORAGE_KEY,
        expect.any(String)
      );
    });
  });

  it('builds collaborator usernames in @username format', async () => {
    mockedUseInvitations.mockReturnValue({
      invitations: [
        {
          id: '1',
          inviterId: '1',
          inviterName: 'Alice Wonderland',
          inviterAvatar: 'avatar-1',
          boardId: '10',
          boardTitle: 'Board A',
          type: 'Board Collaboration',
          status: 'accepted',
          message: '',
          sentAt: '2025-01-01T00:00:00.000Z',
        },
      ],
    } as any);

    const UsernameConsumer: React.FC = () => {
      const { getCollaborators } = useCollaborations();
      const collaborators = getCollaborators();
      return (
        <div data-testid="username">
          {collaborators[0]?.username ?? ''}
        </div>
      );
    };

    render(
      <Wrapper>
        <UsernameConsumer />
      </Wrapper>
    );

    await waitFor(() => {
      expect(screen.getByTestId('username').textContent).toBe('@alicewonderland');
    });
  });

  it('throws an error when useCollaborations is used outside of provider', () => {
    const OrphanConsumer = () => {
      // This will throw at render time because there is no CollaborationProvider
      useCollaborations();
      return null;
    };

    expect(() => render(<OrphanConsumer />)).toThrow(
      'useCollaborations must be used within a CollaborationProvider'
    );
  });
});


