import React, { useMemo, useState, useEffect, useRef } from 'react';
import { Link, useParams, useNavigate } from 'react-router-dom';
import { useUser } from '../context/UserContext';

const FollowersPage: React.FC = () => {
  const { type } = useParams<{ type: 'followers' | 'following' }>();
  const navigate = useNavigate();
  const {
    followers,
    following,
    addToFollowing,
    removeFromFollowing,
    isFollowing: checkIsFollowing,
    loading,
  } = useUser();
  const [sortOption, setSortOption] = useState<'name' | 'recent'>('name');
  const [filterTerm, setFilterTerm] = useState('');
  const [openDropdown, setOpenDropdown] = useState<string | null>(null);
  const dropdownRef = useRef<HTMLDivElement>(null);

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setOpenDropdown(null);
      }
    };

    if (openDropdown) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [openDropdown]);

  const users = type === 'followers' ? followers : following;

  const filteredAndSortedUsers = useMemo(() => {
    let result = [...users];

    // Filter by search term
    if (filterTerm) {
      const lower = filterTerm.toLowerCase();
      result = result.filter(
        (user) =>
          user.name.toLowerCase().includes(lower) ||
          user.username.toLowerCase().includes(lower) ||
          (user.role && user.role.toLowerCase().includes(lower))
      );
    }

    // Sort
    if (sortOption === 'name') {
      result.sort((a, b) => a.name.localeCompare(b.name));
    }

    return result;
  }, [users, sortOption, filterTerm]);

  return (
    <main className="container py-5">
      <button
        type="button"
        className="btn btn-outline-secondary btn-sm mb-4 rounded-pill px-3 shadow-sm"
        onClick={() => navigate(-1)}
      >
        ← Back
      </button>

      <div className="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4 mb-md-5">
        <div>
          <h1 className="h2 mb-1 fw-bold">
            {type === 'followers' ? '👥 Followers' : '➕ Following'}
          </h1>
          <p className="text-muted small mb-0">
            {filteredAndSortedUsers.length} {type === 'followers' ? 'followers' : 'people you follow'}
          </p>
        </div>
        <div className="d-flex gap-2 flex-wrap">
          <input
            type="search"
            className="form-control shadow-sm"
            placeholder="🔍 Search by name or username"
            value={filterTerm}
            onChange={(e) => setFilterTerm(e.target.value)}
            style={{ maxWidth: '300px' }}
          />
          <select
            className="form-select w-auto shadow-sm"
            value={sortOption}
            onChange={(e) => setSortOption(e.target.value as 'name' | 'recent')}
          >
            <option value="name">Sort by name</option>
            <option value="recent">Recently added</option>
          </select>
        </div>
      </div>

      <section className="bg-white rounded-4 rounded-5 shadow-lg p-4 p-md-5">
        {loading ? (
          <div className="text-center py-5 text-muted small">Loading {type}…</div>
        ) : filteredAndSortedUsers.length > 0 ? (
          <div className="d-flex flex-column gap-3">
            {filteredAndSortedUsers.map((user) => (
              <article
                key={user.id}
                className="d-flex align-items-center justify-content-between gap-3 p-3 border-bottom"
              >
                <Link
                  to={`/user/${user.id}`}
                  className="d-flex align-items-center gap-3 text-decoration-none text-reset flex-grow-1"
                >
                  <img
                    src={user.avatarUrl}
                    alt={user.name}
                    className="rounded-circle"
                    style={{ width: '64px', height: '64px', objectFit: 'cover' }}
                  />
                  <div>
                    <p className="mb-0 fw-semibold">{user.name}</p>
                    <small className="text-muted d-block">{user.username}</small>
                    {user.role && <small className="text-muted">{user.role}</small>}
                  </div>
                </Link>
                <div className="d-flex gap-2">
                  {type === 'followers' ? (
                    <button
                      className={`btn btn-sm rounded-pill ${checkIsFollowing(user.id) ? 'btn-outline-secondary' : 'btn-dark'}`}
                      onClick={() => {
                        if (checkIsFollowing(user.id)) {
                          removeFromFollowing(user.id);
                        } else {
                          addToFollowing(user);
                        }
                      }}
                    >
                      {checkIsFollowing(user.id) ? 'Following' : 'Follow back'}
                    </button>
                  ) : (
                    <button
                      className="btn btn-outline-secondary btn-sm rounded-pill"
                      onClick={() => removeFromFollowing(user.id)}
                    >
                      Unfollow
                    </button>
                  )}
                  <div className="position-relative" ref={dropdownRef}>
                    <button
                      className="btn btn-outline-secondary btn-sm rounded-pill"
                      type="button"
                      onClick={() => setOpenDropdown(openDropdown === user.id ? null : user.id)}
                    >
                      ⋯
                    </button>
                    {openDropdown === user.id && (
                      <div
                        className="position-absolute end-0 mt-2 bg-white border rounded shadow-lg"
                        style={{ zIndex: 1000, minWidth: '150px' }}
                      >
                        <Link
                          to={`/user/${user.id}`}
                          className="dropdown-item d-block px-3 py-2 text-decoration-none text-reset"
                          onClick={() => setOpenDropdown(null)}
                        >
                          View profile
                        </Link>
                        <button
                          className="dropdown-item d-block w-100 text-start px-3 py-2 text-danger border-0 bg-transparent"
                          onClick={() => {
                            if (window.confirm(`Block ${user.name}?`)) {
                              alert('User blocked');
                              setOpenDropdown(null);
                            }
                          }}
                        >
                          Block user
                        </button>
                        <button
                          className="dropdown-item d-block w-100 text-start px-3 py-2 text-danger border-0 bg-transparent"
                          onClick={() => {
                            if (window.confirm(`Report ${user.name}?`)) {
                              alert('Thank you for your report');
                              setOpenDropdown(null);
                            }
                          }}
                        >
                          Report user
                        </button>
                      </div>
                    )}
                  </div>
                </div>
              </article>
            ))}
          </div>
        ) : (
          <div className="text-center py-5">
            <p className="text-muted">
              {filterTerm
                ? 'No users found matching your search'
                : `You don't have any ${type === 'followers' ? 'followers' : 'people you follow'} yet`}
            </p>
          </div>
        )}
      </section>
    </main>
  );
};

export default FollowersPage;

