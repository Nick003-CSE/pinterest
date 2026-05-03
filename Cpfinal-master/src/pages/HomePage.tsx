import React, { useEffect, useMemo, useState } from 'react';
import axios from 'axios';
import CategoryFilters from '../components/CategoryFilters';
import HeroBanner from '../components/HeroBanner';
import PinGrid from '../components/PinGrid';
import SpotlightPanel from '../components/SpotlightPanel';
import InvitationsPanel from '../components/InvitationsPanel';
import SponsoredRail from '../components/SponsoredRail';
import BusinessShowcase from '../components/BusinessShowcase';
import { pinCategories, trendingSearches } from '../data/categories';
import { spotlightCollections } from '../data/spotlights';
import { sponsoredPins as fallbackSponsoredPins } from '../data/sponsoredPins';
import { businessProfiles } from '../data/businesses';
import { useSavedPins } from '../context/SavedPinsContext';
import { useAuth } from '../context/AuthContext';
import { Pin } from '../types/pin';
import { API_BASE_URL } from '../config/api';
import { fetchSponsoredPins } from '../services/adsApi';
import { mapSponsoredPinToPin } from '../utils/adsMapper';

const HomePage: React.FC = () => {
  const [activeCategory, setActiveCategory] = useState<string>('All');
  const { savedPins } = useSavedPins();
  const { backendUserId } = useAuth();
  const [remotePins, setRemotePins] = useState<Pin[]>([]);
  const [loadingPins, setLoadingPins] = useState(true);
  const [pinError, setPinError] = useState<string | null>(null);
  const [sponsoredContent, setSponsoredContent] = useState<Pin[]>(fallbackSponsoredPins);
  const [loadingSponsored, setLoadingSponsored] = useState<boolean>(false);
  const [sponsoredError, setSponsoredError] = useState<string | null>(null);

  const mapPinResponse = (pin: any): Pin => {
    const mediaItems = Array.isArray(pin.mediaItems) ? pin.mediaItems : [];
    const primaryImage = pin.mediaUrl || mediaItems[0]?.url;

    return {
      id: String(pin.id),
      ownerId: pin.ownerId ? String(pin.ownerId) : undefined,
      ownerUsername: pin.ownerUsername,
      title: pin.title || 'Untitled Pin',
      description: pin.description || '',
      category: pin.boardName || 'Uncategorized',
      board: pin.boardName || undefined,
      imageUrl: primaryImage,
      author: {
        name: pin.ownerFullName || pin.ownerUsername || 'Creator',
      },
      stats: {
        saves: Number(pin.saveCount ?? 0),
        shares: Number(pin.shareCount ?? 0),
        likes: Number(pin.likeCount ?? 0),
      },
      keywords: Array.isArray(pin.keywords) ? pin.keywords : undefined,
      createdAt: pin.createdAt || new Date().toISOString(),
      attribution: pin.attribution || undefined,
      status: pin.status,
    };
  };

  useEffect(() => {
    const fetchPins = async () => {
      setLoadingPins(true);
      setPinError(null);
      try {
        const response = await axios.get(`${API_BASE_URL}/pins/feed`, {
          params: backendUserId ? { viewerId: backendUserId } : undefined,
        });
        const data = Array.isArray(response.data) ? response.data : [];
        setRemotePins(data.map(mapPinResponse));
      } catch (error) {
        console.error('Failed to load pins for home feed:', error);
        setPinError('Unable to load your feed right now. Please try again soon.');
        setRemotePins([]);
      } finally {
        setLoadingPins(false);
      }
    };

    fetchPins();
  }, [backendUserId]);

  useEffect(() => {
    const fetchSponsoredContent = async () => {
      setLoadingSponsored(true);
      setSponsoredError(null);
      try {
        const sponsored = await fetchSponsoredPins({ limit: 4 });
        if (sponsored.length === 0) {
          setSponsoredContent(fallbackSponsoredPins);
          return;
        }
        setSponsoredContent(sponsored.map(mapSponsoredPinToPin));
      } catch (error) {
        console.error('Failed to load sponsored pins', error);
        setSponsoredError('Sponsored content is temporarily unavailable. Showing saved picks instead.');
        setSponsoredContent(fallbackSponsoredPins);
      } finally {
        setLoadingSponsored(false);
      }
    };

    fetchSponsoredContent();
  }, []);

  const combinedPins = useMemo<Pin[]>(() => {
    const ids = new Set<string>();
    const merged: Pin[] = [];

    remotePins.forEach((pin) => {
      if (!ids.has(pin.id)) {
        merged.push(pin);
        ids.add(pin.id);
      }
    });

    savedPins.forEach((pin) => {
      if (!ids.has(pin.id)) {
        merged.push(pin);
        ids.add(pin.id);
      }
    });

    return merged;
  }, [remotePins, savedPins]);

  const filteredPins = useMemo(() => {
    if (activeCategory === 'All') {
      return combinedPins;
    }
    return combinedPins.filter((pin) => (pin.category || 'Uncategorized') === activeCategory);
  }, [activeCategory, combinedPins]);

  return (
    <main className="container-fluid px-3 px-md-4 px-lg-5 py-4 py-md-5">
      <div className="mb-4 mb-md-5">
        <HeroBanner highlights={trendingSearches} />
      </div>
      <div className="mb-4">
        <CategoryFilters
          categories={pinCategories}
          activeCategory={activeCategory}
          onSelect={setActiveCategory}
        />
      </div>
      <div className="row g-4 g-lg-5 align-items-start">
        <div className="col-12 col-lg-9">
          <div className="mb-4">
            {loadingPins ? (
              <div className="text-center py-5 text-muted">Loading personalized pins…</div>
            ) : pinError ? (
              <div className="alert alert-warning">{pinError}</div>
            ) : filteredPins.length === 0 ? (
              <div className="text-center py-5 text-muted">
                No pins found yet. Create a pin to see it appear here!
              </div>
            ) : (
              <PinGrid pins={filteredPins} />
            )}
          </div>
          {loadingSponsored ? (
            <div className="text-center py-4 text-muted">Loading sponsored campaigns…</div>
          ) : sponsoredError ? (
            <div className="alert alert-light border text-muted">
              {sponsoredError}
            </div>
          ) : (
            <SponsoredRail pins={sponsoredContent} />
          )}
        </div>
        <div className="col-12 col-lg-3">
          <div className="d-flex flex-column gap-4">
            <SpotlightPanel collections={spotlightCollections} />
            <InvitationsPanel />
            <BusinessShowcase businesses={businessProfiles} />
          </div>
        </div>
      </div>
    </main>
  );
};

export default HomePage;

