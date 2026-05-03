import React, { useEffect, useMemo, useState } from 'react';
import axios from 'axios';
import { NavLink, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { API_BASE_URL } from '../config/api';

interface Suggestion {
  id: string;
  label: string;
  type: 'Pin' | 'Board' | 'Keyword';
  thumbnail?: string;
  subtitle?: string;
  pinId?: string;
  boardId?: string;
}

const AppHeader: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const authContext = useAuth();
  const { isAuthenticated, signOut } = authContext;
  const [searchTerm, setSearchTerm] = useState('');
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [showMobileMenu, setShowMobileMenu] = useState(false);
  const [remoteSuggestions, setRemoteSuggestions] = useState<Suggestion[]>([]);
  const [isSuggesting, setIsSuggesting] = useState(false);
  const [suggestionError, setSuggestionError] = useState<string | null>(null);

  // Clear search term when navigating to home page
  useEffect(() => {
    if (location.pathname === '/') {
      setSearchTerm('');
      setShowSuggestions(false);
    }
  }, [location.pathname]);

  useEffect(() => {
    if (searchTerm.trim().length < 2) {
      setRemoteSuggestions([]);
      setSuggestionError(null);
      return;
    }

    const controller = new AbortController();

    const fetchSuggestions = async () => {
      setIsSuggesting(true);
      setSuggestionError(null);
      try {
        const response = await axios.get(`${API_BASE_URL}/pins/suggestions`, {
          params: { keyword: searchTerm.trim() },
          signal: controller.signal,
        });

        const data = response.data || [];
        const mapped: Suggestion[] = data.map((item: any) => ({
          id: item.id || `${item.type}-${item.label}`,
          label: item.label,
          type: (item.type || 'Keyword') as Suggestion['type'],
          thumbnail: item.thumbnail,
          subtitle: item.subtitle,
          pinId: item.pinId,
          boardId: item.boardId,
        }));
        setRemoteSuggestions(mapped);
      } catch (error: any) {
        if (axios.isCancel(error)) return;
        console.error('Failed to fetch suggestions:', error);
        setSuggestionError('Suggestions unavailable');
        setRemoteSuggestions([]);
      } finally {
        setIsSuggesting(false);
      }
    };

    fetchSuggestions();

    return () => controller.abort();
  }, [searchTerm]);

  const filteredSuggestions = useMemo(() => {
    if (searchTerm.length < 1) return [];
    const lower = searchTerm.toLowerCase();
    return remoteSuggestions
      .filter((item) => item.label.toLowerCase().includes(lower))
      .slice(0, 8)
      .sort((a, b) => {
        if (a.type === 'Pin' && b.type !== 'Pin') return -1;
        if (a.type === 'Board' && b.type === 'Keyword') return -1;
        return 0;
      });
  }, [searchTerm, remoteSuggestions]);

  const handleSearch = (event: React.FormEvent) => {
    event.preventDefault();
    if (!searchTerm.trim()) return;
    navigate(`/search?q=${encodeURIComponent(searchTerm.trim())}`);
    setShowSuggestions(false);
  };

  const handleSuggestionClick = (suggestion: Suggestion) => {
    setSearchTerm(suggestion.label);
    if (suggestion.pinId) {
      navigate(`/pin/${suggestion.pinId}`);
    } else if (suggestion.boardId) {
      navigate(`/board/${suggestion.boardId}`);
    } else {
      navigate(`/search?q=${encodeURIComponent(suggestion.label)}`);
    }
    setShowSuggestions(false);
  };

  const handleLogout = () => {
    signOut();
    navigate('/signin');
  };

  const isAuthPage = ['/signin', '/signup'].includes(location.pathname);

  const avatarUrl = useMemo(
    () =>
      'https://d2v5dzhdg4zhx3.cloudfront.net/web-assets/images/storypages/short/linkedin-profile-picture-maker/dummy_image/thumb/004.webp',
    []
  );

  if (isAuthPage) {
    return (
      <header className="auth-brand bg-white border-bottom py-3 text-center">
        <button
          type="button"
          className="brand-pill btn btn-dark rounded-pill px-4 fw-semibold"
          onClick={() => navigate('/')}
        >
          Framescape
        </button>
      </header>
    );
  }

  return (
    <header className="app-header bg-white border-bottom sticky-top">
      <div className="container-fluid px-4 px-lg-5 py-3">
        <div className="d-flex flex-column flex-md-row align-items-start align-items-md-center gap-3 gap-lg-4">
          <div className="d-flex align-items-center gap-3 w-100 w-md-auto justify-content-between">
            <button
              type="button"
              className="brand-logo btn p-0 border-0 bg-transparent"
              onClick={() => navigate('/')}
            >
              <span className="brand-text">Framescape</span>
            </button>

            <nav className="d-none d-md-flex gap-2 align-items-center">
              {[
                { label: 'Home', path: '/' },
                { label: 'Businesses', path: '/businesses' },
                { label: 'Profile', path: '/profile' },
                { label: 'Create', path: '/create' },
              ].map((link) => (
                <NavLink
                  key={link.path}
                  to={link.path}
                  className={({ isActive }) =>
                    `nav-link-btn btn btn-sm rounded-pill px-3 ${isActive ? 'btn-dark text-white' : 'btn-outline-secondary'}`
                  }
                >
                  <span>{link.label}</span>
                </NavLink>
              ))}
            </nav>

            {/* Mobile Menu Button */}
            <button
              type="button"
              className="btn btn-light rounded-circle icon-btn d-md-none"
              onClick={() => setShowMobileMenu(!showMobileMenu)}
              aria-label="Toggle menu"
            >
              {showMobileMenu ? '✕' : '☰'}
            </button>
          </div>

          <form className="search-field flex-grow-1 position-relative" onSubmit={handleSearch}>
            <input
              type="text"
              className="form-control form-control-lg ps-5 shadow-none border-0 bg-body-secondary"
              placeholder="Search ideas"
              aria-label="Search Framescape style inspiration"
              value={searchTerm}
              onChange={(event) => setSearchTerm(event.target.value)}
              onFocus={() => setShowSuggestions(true)}
              onBlur={() => setTimeout(() => setShowSuggestions(false), 120)}
            />
            {showSuggestions && (
            <ul className="search-suggestions list-unstyled position-absolute w-100">
                {isSuggesting && (
                  <li className="px-3 py-2 text-muted small">Loading suggestions…</li>
                )}
                {suggestionError && !isSuggesting && (
                  <li className="px-3 py-2 text-danger small">{suggestionError}</li>
                )}
                {!isSuggesting &&
                  !suggestionError &&
                  filteredSuggestions.map((suggestion) => (
                <li key={suggestion.id}>
                  <button
                    type="button"
                    className="suggestion-btn d-flex align-items-center gap-3 w-100"
                    onMouseDown={() => handleSuggestionClick(suggestion)}
                  >
                    {suggestion.thumbnail && (
                      <img
                        src={suggestion.thumbnail}
                        alt={suggestion.label}
                        className="suggestion-thumbnail rounded"
                      />
                    )}
                    <div className="flex-grow-1 text-start">
                      <div className="d-flex align-items-center gap-2">
                        <span className="fw-semibold">{suggestion.label}</span>
                        <small className="badge bg-body-secondary text-dark">{suggestion.type}</small>
                      </div>
                      {suggestion.subtitle && (
                        <small className="text-muted d-block">{suggestion.subtitle}</small>
                      )}
                    </div>
                  </button>
                </li>
              ))}
                {!isSuggesting && !suggestionError && filteredSuggestions.length === 0 && (
                  <li className="px-3 py-2 text-muted small">No suggestions found</li>
                )}
            </ul>
          )}
          </form>

          <div className="d-flex align-items-center gap-2">
            <button 
              type="button" 
              className="avatar-btn rounded-circle border-0 d-flex align-items-center justify-content-center overflow-hidden p-0"
              title="Profile"
              onClick={() => navigate('/profile')}
            >
              <img
                src={avatarUrl}
                alt={authContext.currentUser?.fullName || 'Profile'}
                className="object-fit-cover rounded-circle"
                style={{ width: 40, height: 40 }}
              />
            </button>
            <button 
              type="button" 
              className="btn btn-outline-secondary btn-sm rounded-pill px-4 logout-btn d-flex align-items-center gap-2" 
              onClick={handleLogout}
              title="Sign out of your account"
            >
              <span>Sign out</span>
            </button>
          </div>
        </div>

        {/* Mobile Navigation Menu */}
        {showMobileMenu && (
          <div className="mobile-nav d-md-none mt-3 pt-3 border-top">
            <nav className="d-flex flex-column gap-2">
              {[
                { label: 'Home', path: '/' },
                { label: 'Businesses', path: '/businesses' },
                { label: 'Profile', path: '/profile' },
                { label: 'Create', path: '/create' },
              ].map((link) => (
                <NavLink
                  key={link.path}
                  to={link.path}
                  className={({ isActive }) =>
                    `nav-link-btn btn btn-sm rounded-pill px-3 ${isActive ? 'btn-dark text-white' : 'btn-outline-secondary'}`
                  }
                  onClick={() => setShowMobileMenu(false)}
                >
                  <span>{link.label}</span>
                </NavLink>
              ))}
            </nav>
          </div>
        )}
      </div>
    </header>
  );
};

export default AppHeader;

