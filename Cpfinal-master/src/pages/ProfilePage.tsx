import React, { useEffect, useMemo, useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import PinGrid from '../components/PinGrid';
import BoardCard from '../components/BoardCard';
import UserList from '../components/UserList';
import CollaborationsList from '../components/CollaborationsList';
import CollaborationInviteModal from '../components/CollaborationInviteModal';
import { currentUser as fallbackUser } from '../data/user';
import { pinCategories } from '../data/categories';
import { useUser } from '../context/UserContext';
import { useInvitations } from '../context/InvitationContext';
import { useSavedPins } from '../context/SavedPinsContext';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Board } from '../types/board';
import { API_BASE_URL } from '../config/api';

type ProfileView = 'pins' | 'drafts' | 'boards';

const ProfilePage: React.FC = () => {
  const navigate = useNavigate();
  const { following, followers, refreshFollowers, refreshFollowing, loading } = useUser();
  const { getPendingInvitations } = useInvitations();
  const { savedPins } = useSavedPins();
  const { backendUserId, currentUser: authUser } = useAuth();

  // Debug: Log followers and following when they change
  useEffect(() => {
    console.log('ProfilePage - Followers count:', followers.length, 'Data:', followers);
    console.log('ProfilePage - Following count:', following.length, 'Data:', following);
    console.log('ProfilePage - backendUserId:', backendUserId);
    console.log('ProfilePage - loading:', loading);
  }, [followers, following, backendUserId, loading]);
  const [activeView, setActiveView] = useState<ProfileView>('pins');
  const [pinSort, setPinSort] = useState<'recent' | 'oldest' | 'alphabetical'>('recent');
  const [pinCategory, setPinCategory] = useState<string>('All');
  const [boardSort, setBoardSort] = useState<'custom' | 'recent' | 'alphabetical'>('custom');
  const [boardCategory, setBoardCategory] = useState<string>('All');
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [boardsState, setBoardsState] = useState<Board[]>([]);
  const [isInviteModalOpen, setIsInviteModalOpen] = useState(false);
  const [selectedBoardForInvite, setSelectedBoardForInvite] = useState<{
    id: string;
    title: string;
  } | null>(null);

  useEffect(() => {
    const fetchBoards = async () => {
      if (!backendUserId) {
        setBoardsState([]);
        return;
      }
      try {
        const res = await axios.get(`${API_BASE_URL}/boards/owner/${backendUserId}`);
        const backendBoards = res.data as any[];
        const mapped: Board[] = backendBoards.map((b) => ({
          id: String(b.id),
          title: b.name,
          description: b.description ?? '',
          category: 'General',
          coverUrl:
            'https://picsum.photos/id/1013/800/600',
          // Use backend pinCount to drive the "X pins" badge
          pinIds: Array.from({ length: b.pinCount ?? 0 }, (_, i) => String(i)),
          pinsPreview: [],
          createdAt: b.createdAt ?? new Date().toISOString(),
          updatedAt:
            b.updatedAt ??
            (b.createdAt ? b.createdAt : new Date().toISOString()),
          collaborators: b.collaborators
            ? b.collaborators.map((c: any) => ({
                id: String(c.id),
                username: c.username,
                fullName: c.fullName,
                avatarUrl: c.avatarUrl,
              }))
            : [],
        }));
        setBoardsState(mapped);
      } catch (err) {
        console.error('Failed to load boards for profile:', err);
        setBoardsState([]);
      }
    };

    fetchBoards();
  }, [backendUserId]);

  // Published pins only (drafts shown separately)
  const pinList = useMemo(() => {
    let results = savedPins.filter((pin) => pin.status !== 'DRAFT');
    if (pinSort === 'alphabetical') {
      results.sort((a, b) => a.title.localeCompare(b.title));
    } else if (pinSort === 'recent') {
      results.sort((a, b) => +new Date(b.createdAt) - +new Date(a.createdAt));
    } else {
      results.sort((a, b) => +new Date(a.createdAt) - +new Date(b.createdAt));
    }
    if (pinCategory !== 'All') {
      results = results.filter((pin) => pin.category === pinCategory);
    }
    if (searchTerm) {
      const lower = searchTerm.toLowerCase();
      results = results.filter(
        (pin) =>
          pin.title.toLowerCase().includes(lower) ||
          pin.description.toLowerCase().includes(lower) ||
          (pin.board ?? '').toLowerCase().includes(lower)
      );
    }
    return results;
  }, [savedPins, pinSort, pinCategory, searchTerm]);

  // Draft pins only
  const draftList = useMemo(() => {
    let results = savedPins.filter((pin) => pin.status === 'DRAFT');
    if (pinSort === 'alphabetical') {
      results.sort((a, b) => a.title.localeCompare(b.title));
    } else if (pinSort === 'recent') {
      results.sort((a, b) => +new Date(b.createdAt) - +new Date(a.createdAt));
    } else {
      results.sort((a, b) => +new Date(a.createdAt) - +new Date(b.createdAt));
    }
    if (pinCategory !== 'All') {
      results = results.filter((pin) => pin.category === pinCategory);
    }
    if (searchTerm) {
      const lower = searchTerm.toLowerCase();
      results = results.filter(
        (pin) =>
          pin.title.toLowerCase().includes(lower) ||
          pin.description.toLowerCase().includes(lower) ||
          (pin.board ?? '').toLowerCase().includes(lower)
      );
    }
    return results;
  }, [savedPins, pinSort, pinCategory, searchTerm]);

  const boardList = useMemo(() => {
    let results = [...boardsState];
    if (boardSort === 'recent') {
      results.sort((a, b) => +new Date(b.createdAt) - +new Date(a.createdAt));
    } else if (boardSort === 'alphabetical') {
      results.sort((a, b) => a.title.localeCompare(b.title));
    }
    if (boardCategory !== 'All') {
      results = results.filter((board) => board.category === boardCategory);
    }
    if (searchTerm) {
      const lower = searchTerm.toLowerCase();
      results = results.filter(
        (board) =>
          board.title.toLowerCase().includes(lower) ||
          board.description.toLowerCase().includes(lower)
      );
    }
    return results;
  }, [boardsState, boardSort, boardCategory, searchTerm]);

  const handleRename = (boardId: string) => {
    const board = boardsState.find((item) => item.id === boardId);
    if (!board) return;
    const newTitle = window.prompt('Rename board', board.title);
    if (newTitle && newTitle.trim()) {
      setBoardsState((prev) =>
        prev.map((item) => (item.id === boardId ? { ...item, title: newTitle } : item))
      );
    }
  };

  const handleDelete = (boardId: string) => {
    if (window.confirm('Delete this board?')) {
      setBoardsState((prev) => prev.filter((item) => item.id !== boardId));
    }
  };

  const handleReorder = (boardId: string, direction: 'up' | 'down') => {
    setBoardsState((prev) => {
      const index = prev.findIndex((item) => item.id === boardId);
      if (index === -1) return prev;
      const targetIndex = direction === 'up' ? index - 1 : index + 1;
      if (targetIndex < 0 || targetIndex >= prev.length) return prev;
      const copy = [...prev];
      [copy[index], copy[targetIndex]] = [copy[targetIndex], copy[index]];
      return copy;
    });
  };

  const handleInviteCollaborators = (boardId: string, boardTitle: string) => {
    if (!backendUserId) {
      alert('Please sign in to invite collaborators.');
      return;
    }
    if (following.length === 0) {
      alert('You are not following anyone yet. Follow people to invite them to collaborate.');
      return;
    }

    setSelectedBoardForInvite({ id: boardId, title: boardTitle });
    setIsInviteModalOpen(true);
  };

  const handleSendInvitations = async (selectedUserIds: string[]) => {
    if (!backendUserId || !selectedBoardForInvite) {
      return;
    }

    try {
      await Promise.all(
        selectedUserIds.map((userId) =>
          axios.post(`${API_BASE_URL}/invitations/${backendUserId}/board-collaboration`, {
            inviteeId: Number(userId),
            boardId: Number(selectedBoardForInvite.id),
            message: `I'd love for you to collaborate on my board "${selectedBoardForInvite.title}".`,
          })
        )
      );
      alert(`Collaboration invitations sent to ${selectedUserIds.length} ${selectedUserIds.length === 1 ? 'person' : 'people'}.`);
    } catch (error) {
      console.error('Failed to send collaboration invitations:', error);
      alert('We could not send all collaboration invitations. Please try again.');
      throw error;
    }
  };

  const profileInfo = {
    name: authUser?.fullName ?? fallbackUser.name,
    username: authUser?.username ?? fallbackUser.username,
    email: authUser?.email ?? 'Email not provided',
    phoneNumber: authUser?.phoneNumber ?? 'Phone not provided',
    bio: fallbackUser.bio,
    location: fallbackUser.location,
    website: fallbackUser.website,
    avatar: fallbackUser.avatarUrl,
    followers,
  };

  return (
    <main className="container py-4 py-md-5 profile-page">
      <section className="profile-hero bg-white rounded-4 rounded-5 shadow-sm p-4 p-md-5 mb-4 mb-md-5">
        <div className="d-flex flex-column flex-md-row gap-4 align-items-center align-items-md-start">
          <img src={profileInfo.avatar} alt={profileInfo.name} className="profile-avatar" />
          <div className="flex-grow-1">
            <div className="d-flex justify-content-between align-items-start flex-wrap gap-3">
              <div>
                <h1 className="h3 mb-1">{profileInfo.name}</h1>
                <p className="text-muted mb-2">{profileInfo.username}</p>
                <p className="mb-3">{profileInfo.bio}</p>
                <p className="small text-muted mb-1">{profileInfo.location}</p>
                <a href={profileInfo.website} className="small">
                  {profileInfo.website}
                </a>
                <div className="mt-3 small text-muted">
                  <p className="mb-1">
                    <strong>Email:</strong> {profileInfo.email}
                  </p>
                  <p className="mb-0">
                    <strong>Phone:</strong> {profileInfo.phoneNumber}
                  </p>
                </div>
              </div>
              <div className="d-flex gap-2 flex-wrap">
                {backendUserId && followers.length === 0 && following.length === 0 && !loading && (
                  <button 
                    className="btn btn-primary rounded-pill px-4 shadow-sm"
                    onClick={async () => {
                      try {
             const response = await axios.post(`${API_BASE_URL}/auth/initialize-followers/${backendUserId}`);
                        console.log('Initialize response:', response.data);
                        alert('Followers initialized! Refreshing...');
                        setTimeout(() => {
                          refreshFollowers();
                          refreshFollowing();
                        }, 500);
                      } catch (error: any) {
                        console.error('Failed to initialize:', error);
                        const errorMsg = error.response?.data?.message || error.message || 'Unknown error';
                        alert(`Failed to initialize: ${errorMsg}\n\nPlease try signing out and signing back in instead.`);
                      }
                    }}
                  >
                    🔄 Initialize Followers
                  </button>
                )}
                <button className="btn btn-dark rounded-pill px-4 shadow-sm">
                  <span className="me-2">📤</span> Share profile
                </button>
                <Link to="/invitations" className="btn btn-outline-secondary rounded-pill position-relative px-4">
                  <span className="me-2">✉️</span> Invitations
                  {getPendingInvitations().length > 0 && (
                    <span className="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger shadow-sm" style={{ fontSize: '0.65rem' }}>
                      {getPendingInvitations().length}
                    </span>
                  )}
                </Link>
              </div>
            </div>
            <div className="profile-stats d-flex gap-4 gap-md-5 mt-4 flex-wrap">
              <button
                className="stat-item text-center p-3 rounded-3 bg-light bg-opacity-50 border-0 w-auto"
                onClick={() => setActiveView('pins')}
                type="button"
              >
                <p className="h3 mb-0 fw-bold text-primary">{savedPins.length}</p>
                <small className="text-muted fw-medium">Pins</small>
              </button>
              <button
                className="stat-item text-center p-3 rounded-3 bg-light bg-opacity-50 border-0 w-auto"
                onClick={() => setActiveView('boards')}
                type="button"
              >
                <p className="h3 mb-0 fw-bold text-primary">{boardsState.length}</p>
                <small className="text-muted fw-medium">Boards</small>
              </button>
              <div
                className="stat-item clickable-stat text-center p-3 rounded-3 bg-light bg-opacity-50"
                onClick={() => navigate('/followers/followers')}
                role="button"
                tabIndex={0}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    navigate('/followers/followers');
                  }
                }}
              >
                <p className="h3 mb-0 fw-bold text-primary">{followers.length}</p>
                <small className="text-muted fw-medium">Followers</small>
              </div>
              <div
                className="stat-item clickable-stat text-center p-3 rounded-3 bg-light bg-opacity-50"
                onClick={() => navigate('/followers/following')}
                role="button"
                tabIndex={0}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    navigate('/followers/following');
                  }
                }}
              >
                <p className="h3 mb-0 fw-bold text-primary">{following.length}</p>
                <small className="text-muted fw-medium">Following</small>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="profile-controls d-flex flex-column flex-xl-row gap-3 mb-4 align-items-xl-center">
        <div className="btn-group">
          <button
            className={`btn btn-outline-dark ${activeView === 'pins' ? 'active' : ''}`}
            onClick={() => setActiveView('pins')}
          >
            Pins
          </button>
          <button
            className={`btn btn-outline-dark ${activeView === 'drafts' ? 'active' : ''}`}
            onClick={() => setActiveView('drafts')}
          >
            Drafts
          </button>
          <button
            className={`btn btn-outline-dark ${activeView === 'boards' ? 'active' : ''}`}
            onClick={() => setActiveView('boards')}
          >
            Boards
          </button>
        </div>
        <input
          type="search"
          className="form-control flex-grow-1"
          placeholder={`Search ${activeView}`}
          value={searchTerm}
          onChange={(event) => setSearchTerm(event.target.value)}
        />
        {activeView === 'pins' ? (
          <>
            <select
              className="form-select w-auto"
              value={pinSort}
              onChange={(event) => setPinSort(event.target.value as typeof pinSort)}
            >
              <option value="recent">Newest first</option>
              <option value="oldest">Oldest first</option>
              <option value="alphabetical">A-Z</option>
            </select>
            <select
              className="form-select w-auto"
              value={pinCategory}
              onChange={(event) => setPinCategory(event.target.value)}
            >
              <option value="All">All categories</option>
              {pinCategories.map((category) => (
                <option key={category} value={category}>
                  {category}
                </option>
              ))}
            </select>
          </>
        ) : (
          <>
            <select
              className="form-select w-auto"
              value={boardSort}
              onChange={(event) => setBoardSort(event.target.value as typeof boardSort)}
            >
              <option value="custom">Custom order</option>
              <option value="recent">Newest first</option>
              <option value="alphabetical">A-Z</option>
            </select>
            <select
              className="form-select w-auto"
              value={boardCategory}
              onChange={(event) => setBoardCategory(event.target.value)}
            >
              <option value="All">All categories</option>
              {Array.from(new Set(boardsState.map((board) => board.category))).map((category) => (
                <option key={category} value={category}>
                  {category}
                </option>
              ))}
            </select>
          </>
        )}
      </section>
      {activeView === 'pins' && <PinGrid pins={pinList} />}
      {activeView === 'drafts' && <PinGrid pins={draftList} />}
      {activeView === 'boards' && (
        <div className="row g-4">
          {boardList.map((board) => (
            <div key={board.id} className="col-12 col-md-6">
              <BoardCard
                board={board}
                actions={
                  <>
                    <button
                      className="btn btn-outline-secondary btn-sm rounded-pill"
                      onClick={() => handleRename(board.id)}
                    >
                      Rename
                    </button>
                    <button
                      className="btn btn-outline-secondary btn-sm rounded-pill"
                      onClick={() => handleReorder(board.id, 'up')}
                    >
                      Move up
                    </button>
                    <button
                      className="btn btn-outline-secondary btn-sm rounded-pill"
                      onClick={() => handleReorder(board.id, 'down')}
                    >
                      Move down
                    </button>
                    <button
                      className="btn btn-outline-secondary btn-sm rounded-pill"
                      onClick={() => handleInviteCollaborators(board.id, board.title)}
                    >
                      Invite collaborators
                    </button>
                    <button
                      className="btn btn-outline-danger btn-sm rounded-pill"
                      onClick={() => handleDelete(board.id)}
                    >
                      Delete
                    </button>
                  </>
                }
              />
            </div>
          ))}
        </div>
      )}

      <div className="row g-4 mt-4">
        <div className="col-12 col-lg-4">
          <UserList
            title={`Followers ${loading ? '(loading...)' : `(${followers.length})`}`}
            users={followers}
            actionLabel="View all"
            onViewAll={() => navigate('/followers/followers')}
          />
          {!loading && !backendUserId && (
            <div className="alert alert-warning mt-2 small">
              Please sign in to see your followers
            </div>
          )}
        </div>
        <div className="col-12 col-lg-4">
          <UserList
            title={`Following ${loading ? '(loading...)' : `(${following.length})`}`}
            users={following}
            actionLabel="View all"
            onViewAll={() => navigate('/followers/following')}
          />
          {!loading && !backendUserId && (
            <div className="alert alert-warning mt-2 small">
              Please sign in to see who you're following
            </div>
          )}
        </div>
        <div className="col-12 col-lg-4">
          <CollaborationsList />
        </div>
      </div>
      {selectedBoardForInvite && (
        <CollaborationInviteModal
          isOpen={isInviteModalOpen}
          onClose={() => {
            setIsInviteModalOpen(false);
            setSelectedBoardForInvite(null);
          }}
          following={following}
          boardTitle={selectedBoardForInvite.title}
          onInvite={handleSendInvitations}
        />
      )}
    </main>
  );
};

export default ProfilePage;

