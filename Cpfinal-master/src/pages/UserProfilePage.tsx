import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import PinGrid from '../components/PinGrid';
import { pins } from '../data/pins';
import { useUser } from '../context/UserContext';
import { UserSummary } from '../types/user';

const UserProfilePage: React.FC = () => {
  const { userId } = useParams<{ userId: string }>();
  const navigate = useNavigate();
  const { addToFollowing, removeFromFollowing, isFollowing: checkIsFollowing, followers, following, getFollowersForUser, getFollowingForUser } =
    useUser();
  const [isBlocked, setIsBlocked] = useState(false);
  const [openDropdown, setOpenDropdown] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const [userFollowers, setUserFollowers] = useState<UserSummary[]>([]);
  const [userFollowing, setUserFollowing] = useState<UserSummary[]>([]);
  const [loadingStats, setLoadingStats] = useState(false);

  const fetchUserStats = useCallback(async () => {
    if (!userId) return;
    setLoadingStats(true);
    try {
      const [followersData, followingData] = await Promise.all([
        getFollowersForUser(userId),
        getFollowingForUser(userId),
      ]);
      setUserFollowers(followersData);
      setUserFollowing(followingData);
    } catch (error) {
      console.error('Failed to load user stats:', error);
    } finally {
      setLoadingStats(false);
    }
  }, [userId, getFollowersForUser, getFollowingForUser]);

  // Fetch followers and following for the viewed user
  useEffect(() => {
    fetchUserStats();
  }, [fetchUserStats]);

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setOpenDropdown(false);
      }
    };

    if (openDropdown) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [openDropdown]);

  // In a real app, this would fetch user data by ID
  // For now, we'll use a mock user or find from followers/following
  const knownUsers = [...followers, ...following];
  const user =
    knownUsers.find((u) => u.id === userId) || {
      id: userId || 'unknown',
      name: 'Sample User',
      username: '@sampleuser',
      avatarUrl: 'https://picsum.photos/id/68/200/200',
      role: 'Designer',
    };

  const userPins = pins.slice(0, 6); // Mock pins for the user
  const isFollowing = checkIsFollowing(user.id);

  const handleFollow = async () => {
    if (isFollowing) {
      await removeFromFollowing(user.id);
    } else {
      await addToFollowing(user);
    }

    // Refresh this user's follower/following stats so counts update immediately
    await fetchUserStats();
  };

  const handleBlock = () => {
    if (window.confirm('Are you sure you want to block this user?')) {
      setIsBlocked(true);
      // Remove from following if currently following
      if (isFollowing) {
        removeFromFollowing(user.id);
      }
    }
  };

  const handleReport = () => {
    if (window.confirm('Report this user for inappropriate content?')) {
      alert('Thank you for your report. We will review it shortly.');
    }
  };

  if (isBlocked) {
    return (
      <main className="container py-5">
        <div className="bg-white rounded-4 shadow-sm p-5 text-center">
          <p className="text-muted">This user has been blocked.</p>
          <button className="btn btn-outline-secondary mt-3" onClick={() => setIsBlocked(false)}>
            Unblock user
          </button>
        </div>
      </main>
    );
  }

  return (
    <main className="container py-4 py-md-5 profile-page">
      <button
        type="button"
        className="btn btn-outline-secondary btn-sm mb-4 rounded-pill px-3 shadow-sm"
        onClick={() => navigate(-1)}
      >
        ← Back
      </button>

      <section className="profile-hero bg-white rounded-4 rounded-5 shadow-lg p-4 p-md-5 mb-4 mb-md-5">
        <div className="d-flex flex-column flex-md-row gap-4 align-items-center align-items-md-start">
          <img
            src={user.avatarUrl}
            alt={user.name}
            className="profile-avatar"
          />
          <div className="flex-grow-1">
            <div className="d-flex justify-content-between align-items-start flex-wrap gap-3">
              <div>
                <h1 className="h3 mb-1">{user.name}</h1>
                <p className="text-muted mb-2">{user.username}</p>
                {user.role && <p className="mb-3">{user.role}</p>}
              </div>
              <div className="d-flex gap-2 flex-wrap">
                <button
                  className={`btn rounded-pill px-4 shadow-sm ${isFollowing ? 'btn-outline-secondary' : 'btn-dark'}`}
                  onClick={handleFollow}
                >
                  {isFollowing ? '✓ Following' : '➕ Follow'}
                </button>
                <div className="position-relative" ref={dropdownRef}>
                  <button
                    className="btn btn-outline-secondary rounded-pill px-3 shadow-sm"
                    type="button"
                    onClick={() => setOpenDropdown(!openDropdown)}
                  >
                    ⋯
                  </button>
                  {openDropdown && (
                    <div
                      className="position-absolute end-0 mt-2 bg-white border rounded shadow-lg"
                      style={{ zIndex: 1000, minWidth: '150px' }}
                    >
                      <button
                        className="dropdown-item d-block w-100 text-start px-3 py-2 border-0 bg-transparent"
                        onClick={() => {
                          alert('Profile link copied!');
                          setOpenDropdown(false);
                        }}
                      >
                        Share profile
                      </button>
                      <hr className="my-1" />
                      <button
                        className="dropdown-item d-block w-100 text-start px-3 py-2 text-danger border-0 bg-transparent"
                        onClick={() => {
                          handleBlock();
                          setOpenDropdown(false);
                        }}
                      >
                        Block user
                      </button>
                      <button
                        className="dropdown-item d-block w-100 text-start px-3 py-2 text-danger border-0 bg-transparent"
                        onClick={() => {
                          handleReport();
                          setOpenDropdown(false);
                        }}
                      >
                        Report user
                      </button>
                    </div>
                  )}
                </div>
              </div>
            </div>
            <div className="d-flex gap-4 mt-4 flex-wrap">
              <div>
                <p className="h4 mb-0">{userPins.length}</p>
                <small className="text-muted">Pins</small>
              </div>
              <div>
                <p className="h4 mb-0">{loadingStats ? '...' : userFollowers.length}</p>
                <small className="text-muted">Followers</small>
              </div>
              <div>
                <p className="h4 mb-0">{loadingStats ? '...' : userFollowing.length}</p>
                <small className="text-muted">Following</small>
              </div>
            </div>
          </div>
        </div>
      </section>

      <div className="d-flex gap-3 mb-4">
        <Link
          to={`/followers/${user.id}/followers`}
          className="btn btn-outline-secondary rounded-pill"
        >
          View Followers
        </Link>
        <Link
          to={`/followers/${user.id}/following`}
          className="btn btn-outline-secondary rounded-pill"
        >
          View Following
        </Link>
      </div>

      <section>
        <h2 className="h5 mb-4">Pins</h2>
        <PinGrid pins={userPins} />
      </section>
    </main>
  );
};

export default UserProfilePage;

