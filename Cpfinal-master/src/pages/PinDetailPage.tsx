import React, { useEffect, useMemo, useRef, useState } from 'react';
import { Link, useParams, useNavigate, useLocation } from 'react-router-dom';
import axios from 'axios';
import { pins } from '../data/pins';
import { sponsoredPins as fallbackSponsoredPins } from '../data/sponsoredPins';
import { boards } from '../data/boards';
import PinGrid from '../components/PinGrid';
import { useSavedPins } from '../context/SavedPinsContext';
import { Pin } from '../types/pin';
import { API_BASE_URL } from '../config/api';
import { fetchSponsoredPins } from '../services/adsApi';
import { mapSponsoredPinToPin } from '../utils/adsMapper';
import { useAuth } from '../context/AuthContext';
import { useUser } from '../context/UserContext';

const PinDetailPage: React.FC = () => {
  const { pinId } = useParams();
  const navigate = useNavigate();
  const [isLiked, setIsLiked] = useState(false);
  const [loading, setLoading] = useState(false);
  const [pinError, setPinError] = useState<string | null>(null);
  const { currentUser, backendUserId } = useAuth();
  const { addToFollowing, removeFromFollowing, isFollowing } = useUser();
  const location = useLocation();
  const locationState = location.state as { pin?: Pin } | undefined;
  const preloadedPin = locationState?.pin;
  const [dynamicPin, setDynamicPin] = useState<Pin | null>(preloadedPin ?? null);
  const { isPinSaved, savePin, unsavePin, savedPins, refreshPins } = useSavedPins();
  const isSaved = isPinSaved(pinId || '');
  const [engagement, setEngagement] = useState({ saves: 0, shares: 0, likes: 0 });
  const [sponsoredSuggestions, setSponsoredSuggestions] = useState<Pin[]>(fallbackSponsoredPins);
  const [loadingSponsored, setLoadingSponsored] = useState<boolean>(false);
  const [sponsoredError, setSponsoredError] = useState<string | null>(null);
  const [isLiking, setIsLiking] = useState<boolean>(false);
  const [hasIncrementedSave, setHasIncrementedSave] = useState<boolean>(false);
  const [hasIncrementedShare, setHasIncrementedShare] = useState<boolean>(false);
  const lastPinIdRef = useRef<string | null>(null);
  const isLikingRef = useRef<boolean>(false); // Synchronous ref to prevent race conditions

  // Look for pin in backend-loaded saved pins first, then static data
  const staticOrSavedPin = useMemo(
    () =>
      savedPins.find((item) => item.id === pinId) ||
      pins.find((item) => item.id === pinId) ||
      sponsoredSuggestions.find((item) => item.id === pinId),
    [savedPins, pinId, sponsoredSuggestions]
  );

  const pin = dynamicPin || staticOrSavedPin;

  useEffect(() => {
    if (preloadedPin) {
      setDynamicPin(preloadedPin);
    }
  }, [preloadedPin]);

  useEffect(() => {
    const fetchPin = async () => {
      if (!pinId) {
        return;
      }
      try {
        setLoading(true);
        setPinError(null);
        const url = backendUserId
          ? `${API_BASE_URL}/pins/${pinId}?userId=${backendUserId}`
          : `${API_BASE_URL}/pins/${pinId}`;
        const response = await axios.get(url);
        const p = response.data;
        setIsLiked(p.isLiked ?? false);
        setDynamicPin({
          id: String(p.id),
          ownerId: p.ownerId ? String(p.ownerId) : undefined,
          ownerUsername: p.ownerUsername,
          title: p.title,
          description: p.description ?? '',
          category: p.boardName || 'Uncategorized',
          imageUrl: p.mediaUrl,
          author: {
            name: p.ownerFullName || p.ownerUsername || 'Unknown',
          },
          board: p.boardName,
          stats: {
            saves: Number(p.saveCount ?? 0),
            shares: Number(p.shareCount ?? 0),
            likes: Number(p.likeCount ?? 0),
          },
          createdAt: p.createdAt ?? new Date().toISOString(),
          attribution: p.attribution,
          status: p.status,
        });
      } catch (error) {
        console.error('Failed to load pin by id:', error);
        setPinError('We could not load this Pin from the server.');
      } finally {
        setLoading(false);
      }
    };
    fetchPin();
  }, [pinId, backendUserId]);

  useEffect(() => {
    if (!pin) {
      setEngagement({ saves: 0, shares: 0, likes: 0 });
      lastPinIdRef.current = null;
      setHasIncrementedSave(false);
      setIsLiked(false);
      isLikingRef.current = false; // Reset like ref
      return;
    }

    setEngagement({
      saves: pin.stats?.saves ?? 0,
      shares: pin.stats?.shares ?? 0,
      likes: pin.stats?.likes ?? 0,
    });

    if (pin.id !== lastPinIdRef.current) {
      setHasIncrementedSave(false);
      setHasIncrementedShare(false);
      isLikingRef.current = false; // Reset like ref when pin changes
      lastPinIdRef.current = pin.id;
    }
  }, [pin]);

  useEffect(() => {
    const loadSponsoredPins = async () => {
      setLoadingSponsored(true);
      setSponsoredError(null);
      try {
        const response = await fetchSponsoredPins({ limit: 6 });
        if (response.length === 0) {
          setSponsoredSuggestions(fallbackSponsoredPins);
        } else {
          setSponsoredSuggestions(response.map(mapSponsoredPinToPin));
        }
      } catch (error) {
        console.error('Failed to load sponsored pin suggestions', error);
        setSponsoredError('Sponsored suggestions are temporarily unavailable.');
        setSponsoredSuggestions(fallbackSponsoredPins);
      } finally {
        setLoadingSponsored(false);
      }
    };

    loadSponsoredPins();
  }, []);

  const otherSponsoredPins = useMemo(() => {
    if (!pin) {
      return sponsoredSuggestions.slice(0, 6);
    }
    return sponsoredSuggestions.filter((p) => p.id !== pin.id).slice(0, 6);
  }, [sponsoredSuggestions, pin]);

  const canEdit = Boolean(pin?.ownerId && currentUser?.userId && String(currentUser.userId) === pin.ownerId);
  const canFollow =
    Boolean(pin?.ownerId) &&
    Boolean(currentUser?.userId) &&
    String(currentUser!.userId) !== pin?.ownerId;
  const isCreatorFollowed =
    canFollow && pin?.ownerId ? isFollowing(pin.ownerId) : false;

  if (loading && !pin) {
    return (
      <main className="container py-5">
        <p>Loading pin…</p>
      </main>
    );
  }

  if (!pin) {
    return (
      <main className="container py-5">
        <p>
          {pinError ?? "We couldn't find that Pin."}{' '}
          <Link to="/">Go back home.</Link>
        </p>
      </main>
    );
  }

  const relatedBoard = boards.find((board) => board.title === pin.board);
  const relatedPins = relatedBoard
    ? pins.filter((p) => relatedBoard.pinIds.includes(p.id) && p.id !== pin.id).slice(0, 6)
    : [];

  return (
    <main className="container py-4 py-md-5 pin-detail-page">
      <button
        type="button"
        className="btn btn-outline-secondary btn-sm mb-4 rounded-pill px-3 shadow-sm"
        onClick={() => navigate(-1)}
      >
        ← Back
      </button>

      <div className="row g-4 g-md-5 mb-5">
        <div className="col-12 col-lg-7">
          <div className="pin-detail-media rounded-4 rounded-5 overflow-hidden shadow-lg bg-white position-relative">
            {pin.sponsored && (
              <span className="badge bg-warning text-dark position-absolute top-0 start-0 m-3 shadow-sm px-3 py-2">
                ⭐ Sponsored {pin.sponsorName && `by ${pin.sponsorName}`}
              </span>
            )}
            <img src={pin.imageUrl} alt={pin.title} className="w-100 h-100 object-fit-cover" />
          </div>
        </div>
        <div className="col-12 col-lg-5">
          <section className="bg-white rounded-4 rounded-5 shadow-lg p-4 p-md-5 d-flex flex-column gap-4">
            <div>
              <span className="badge bg-body-secondary text-dark mb-3 px-3 py-2">{pin.category}</span>
              <h1 className="h2 mb-3 fw-bold">{pin.title}</h1>
              <p className="lead mb-3 lh-base">{pin.description}</p>
              <p className="text-muted small mb-0">
                Saved to{' '}
                {relatedBoard ? (
                  <Link to={`/board/${relatedBoard.id}`} className="text-decoration-none fw-semibold">
                    {relatedBoard.title}
                  </Link>
                ) : (
                  pin.board || 'Unsorted ideas'
                )}
              </p>
            </div>

            {pin.palette && pin.palette.length > 0 && (
              <section>
                <p className="text-muted small mb-2 fw-semibold">Color Palette</p>
                <div className="d-flex gap-2 flex-wrap">
                  {pin.palette.map((color) => (
                    <div key={color} className="d-flex align-items-center gap-2">
                      <span
                        className="palette-dot rounded-pill"
                        style={{ backgroundColor: color, width: '32px', height: '32px' }}
                        aria-label={color}
                      />
                      <small className="text-muted">{color}</small>
                    </div>
                  ))}
                </div>
              </section>
            )}

            <section className="border-top pt-4">
              <p className="text-muted small mb-3 fw-semibold">Created by</p>
              <div className="d-flex align-items-center justify-content-between gap-3 flex-wrap">
                <div className="d-flex align-items-center gap-3">
                  {pin.author.avatarUrl && (
                    <img
                      src={pin.author.avatarUrl}
                      alt={pin.author.name}
                      className="rounded-circle"
                      style={{ width: '56px', height: '56px', objectFit: 'cover' }}
                    />
                  )}
                  <div>
                    <p className="fw-semibold mb-1 mb-md-0">{pin.author.name}</p>
                    {pin.author.location && <p className="text-muted small mb-0">{pin.author.location}</p>}
                  </div>
                </div>
                {canFollow && pin.ownerId && (
                  <button
                    type="button"
                    className={`btn btn-sm rounded-pill px-3 shadow-sm ${
                      isCreatorFollowed ? 'btn-outline-secondary' : 'btn-dark'
                    }`}
                    onClick={async () => {
                      try {
                        if (isCreatorFollowed) {
                          await removeFromFollowing(pin.ownerId!);
                        } else {
                          await addToFollowing({
                            id: pin.ownerId!,
                            name: pin.author.name,
                            username: pin.ownerUsername || '',
                            avatarUrl: pin.author.avatarUrl || '',
                          });
                        }
                      } catch (error) {
                        console.error('Failed to toggle follow state from pin detail:', error);
                      }
                    }}
                  >
                    {isCreatorFollowed ? '✓ Following' : 'Follow'}
                  </button>
                )}
              </div>
            </section>

            <section className="border-top pt-4">
              <div className="row g-3">
                <div className="col-4">
                  <p className="text-muted small mb-1">Saves</p>
                  <p className="h5 mb-0">{engagement.saves.toLocaleString()}</p>
                </div>
                <div className="col-4">
                  <p className="text-muted small mb-1">Likes</p>
                  <p className="h5 mb-0">{engagement.likes.toLocaleString()}</p>
                </div>
                <div className="col-4">
                  <p className="text-muted small mb-1">Shares</p>
                  <p className="h5 mb-0">{engagement.shares.toLocaleString()}</p>
                </div>
              </div>
            </section>

            {pin.sponsored && pin.sponsorWebsite && (
              <section className="border-top pt-4">
                <p className="text-muted small mb-2 fw-semibold">Advertising Campaign</p>
                <p className="small mb-3">{pin.attribution || `Sponsored content from ${pin.sponsorName}`}</p>
                <a
                  href={pin.sponsorWebsite}
                  target="_blank"
                  rel="noreferrer"
                  className="btn btn-dark btn-sm rounded-pill"
                >
                  Visit {pin.sponsorName} Website →
                </a>
              </section>
            )}

            {pin.attribution && !pin.sponsored && (
              <section className="border-top pt-4">
                <p className="text-muted small mb-2 fw-semibold">Attribution</p>
                <p className="small mb-0">{pin.attribution}</p>
              </section>
            )}

            <section className="border-top pt-4">
              <p className="text-muted small mb-2 fw-semibold">Details</p>
              <div className="d-flex flex-column gap-2 small">
                <div className="d-flex justify-content-between">
                  <span className="text-muted">Saved on</span>
                  <span className="fw-semibold">{new Date(pin.createdAt).toLocaleDateString('en-US', {
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric'
                  })}</span>
                </div>
                <div className="d-flex justify-content-between">
                  <span className="text-muted">Category</span>
                  <span className="fw-semibold">{pin.category}</span>
                </div>
              </div>
            </section>

            <div className="d-flex gap-2 flex-wrap pt-2">
              <button
                className={`btn rounded-pill flex-grow-1 shadow-sm ${isSaved ? 'btn-dark' : 'btn-outline-secondary'}`}
                onClick={async () => {
                  if (!pin || !pinId) {
                    return;
                  }
                  if (isSaved) {
                    unsavePin(pin.id);
                    return;
                  }

                  savePin(pin);
                  if (hasIncrementedSave) {
                    return;
                  }

                  try {
                    const response = await axios.post(
                      `${API_BASE_URL}/pins/${pinId}/save`
                    );
                    const data = response.data;
                    setEngagement({
                      saves: Number(data.saveCount ?? engagement.saves),
                      shares: Number(data.shareCount ?? engagement.shares),
                      likes: Number(data.likeCount ?? engagement.likes),
                    });
                    setDynamicPin((prev) => {
                      const base = prev ?? pin;
                      if (!base) {
                        return prev;
                      }
                      return {
                        ...base,
                        stats: {
                          ...base.stats,
                          saves: Number(data.saveCount ?? engagement.saves),
                          shares: Number(data.shareCount ?? engagement.shares),
                          likes: Number(data.likeCount ?? engagement.likes),
                        },
                      };
                    });
                    setHasIncrementedSave(true);
                  } catch (error) {
                    console.error('Failed to increment save count:', error);
                  }
                }}
              >
                {isSaved ? '✓ Saved' : '💾 Save'}
              </button>
              <button
                className={`btn rounded-pill flex-grow-1 shadow-sm ${isLiked ? 'btn-danger' : 'btn-outline-secondary'}`}
                disabled={isLiking}
                onClick={async () => {
                  if (!pinId || !backendUserId) {
                    alert('Please sign in to like pins.');
                    return;
                  }
                  
                  // Synchronous check using ref to prevent race conditions
                  if (isLikingRef.current) {
                    return; // Prevent multiple simultaneous requests
                  }
                  
                  // Set both state and ref immediately
                  isLikingRef.current = true;
                  setIsLiking(true);
                  
                  // Optimistically update the UI
                  const wasLiked = isLiked;
                  const newLikeCount = wasLiked ? engagement.likes - 1 : engagement.likes + 1;
                  setIsLiked(!wasLiked);
                  setEngagement((prev) => ({
                    ...prev,
                    likes: newLikeCount,
                  }));
                  setDynamicPin((prev) => {
                    const base = prev ?? pin;
                    if (!base) {
                      return prev;
                    }
                    return {
                      ...base,
                      stats: {
                        ...base.stats,
                        likes: newLikeCount,
                      },
                    };
                  });
                  
                  try {
                    const response = await axios.post(
                      `${API_BASE_URL}/pins/${pinId}/like?userId=${backendUserId}`
                    );
                    const data = response.data;
                    console.log('Like response:', data);
                    // Update with actual backend response
                    const actualLikeCount = Number(data.likeCount ?? data.like_count ?? newLikeCount);
                    setEngagement({
                      saves: Number(data.saveCount ?? data.save_count ?? engagement.saves),
                      shares: Number(data.shareCount ?? data.share_count ?? engagement.shares),
                      likes: actualLikeCount,
                    });
                    setIsLiked(data.isLiked ?? !wasLiked);
                    setDynamicPin((prev) => {
                      const base = prev ?? pin;
                      if (!base) {
                        return prev;
                      }
                      return {
                        ...base,
                        stats: {
                          ...base.stats,
                          saves: Number(data.saveCount ?? data.save_count ?? engagement.saves),
                          shares: Number(data.shareCount ?? data.share_count ?? engagement.shares),
                          likes: actualLikeCount,
                        },
                      };
                    });
                  } catch (error) {
                    console.error('Failed to toggle like:', error);
                    // Revert on error
                    setEngagement((prev) => ({
                      ...prev,
                      likes: wasLiked ? prev.likes + 1 : prev.likes - 1,
                    }));
                    setIsLiked(wasLiked);
                    setDynamicPin((prev) => {
                      const base = prev ?? pin;
                      if (!base) {
                        return prev;
                      }
                      return {
                        ...base,
                        stats: {
                          ...base.stats,
                          likes: wasLiked ? base.stats.likes + 1 : base.stats.likes - 1,
                        },
                      };
                    });
                  } finally {
                    // Reset both state and ref
                    isLikingRef.current = false;
                    setIsLiking(false);
                  }
                }}
              >
                {isLiking ? (
                  <>
                    <span
                      className="spinner-border spinner-border-sm me-2"
                      role="status"
                      aria-hidden="true"
                    />
                    {isLiked ? 'Liking...' : 'Unliking...'}
                  </>
                ) : (
                  isLiked ? `❤️ Liked (${engagement.likes})` : `🤍 Like (${engagement.likes})`
                )}
              </button>
              <button
                className="btn btn-outline-secondary rounded-pill flex-grow-1 shadow-sm"
                onClick={async () => {
                  if (!pinId) return;
                  if (hasIncrementedShare) {
                    return;
                  }
                  try {
                    const response = await axios.post(
                      `${API_BASE_URL}/pins/${pinId}/share`
                    );
                    const data = response.data;
                    setEngagement({
                      saves: Number(data.saveCount ?? engagement.saves),
                      shares: Number(data.shareCount ?? engagement.shares),
                      likes: Number(data.likeCount ?? engagement.likes),
                    });
                    setDynamicPin((prev) => {
                      const base = prev ?? pin;
                      if (!base) {
                        return prev;
                      }
                      return {
                        ...base,
                        stats: {
                          ...base.stats,
                          saves: Number(data.saveCount ?? engagement.saves),
                          shares: Number(data.shareCount ?? engagement.shares),
                          likes: Number(data.likeCount ?? engagement.likes),
                        },
                      };
                    });
                    setHasIncrementedShare(true);
                  } catch (error) {
                    console.error('Failed to increment share count:', error);
                  }
                }}
              >
                📤 Share
              </button>
              {pinId && canEdit && (
                <button
                  className="btn btn-outline-danger rounded-pill flex-grow-1 shadow-sm"
                  onClick={async () => {
                    if (!window.confirm('Delete this pin? This cannot be undone.')) return;
                    if (!backendUserId) {
                      alert('Please sign in to delete pins.');
                      return;
                    }
                    try {
                      await axios.delete(`${API_BASE_URL}/pins/${pinId}`, {
                        params: { userId: backendUserId }
                      });
                      // Update local state
                      unsavePin(String(pinId));
                      await refreshPins();
                      navigate(-1);
                    } catch (error) {
                      console.error('Failed to delete pin:', error);
                      if (axios.isAxiosError(error) && error.response?.status === 400) {
                        alert('You can only delete your own pins.');
                      } else {
                        alert('Could not delete this pin. Please try again.');
                      }
                    }
                  }}
                >
                  🗑 Delete pin
                </button>
              )}
              {canEdit && pinId && (
                <button
                  className="btn btn-outline-primary rounded-pill flex-grow-1 shadow-sm"
                  onClick={() => navigate(`/pin/${pinId}/edit`, { state: { pin } })}
                >
                  ✏️ Edit pin
                </button>
              )}
            </div>
          </section>
        </div>
      </div>

      {pin.sponsored ? (
        <section className="mt-5">
          <div className="d-flex justify-content-between align-items-center mb-4">
            <h2 className="h4 mb-0">More Sponsored Content</h2>
            <Link to="/campaigns" className="btn btn-outline-secondary btn-sm">
              View all campaigns
            </Link>
          </div>
          {loadingSponsored ? (
            <div className="text-center py-4 text-muted">Loading sponsored suggestions…</div>
          ) : otherSponsoredPins.length > 0 ? (
            <PinGrid pins={otherSponsoredPins} />
          ) : (
            <div className="alert alert-light border text-muted">
              {sponsoredError || 'No additional sponsored content is available right now.'}
            </div>
          )}
        </section>
      ) : relatedPins.length > 0 && (
        <section className="mt-5">
          <div className="d-flex justify-content-between align-items-center mb-4">
            <h2 className="h4 mb-0">More from this board</h2>
            {relatedBoard && (
              <Link to={`/board/${relatedBoard.id}`} className="btn btn-outline-secondary btn-sm">
                View all pins
              </Link>
            )}
          </div>
          <PinGrid pins={relatedPins} />
        </section>
      )}
    </main>
  );
};

export default PinDetailPage;

