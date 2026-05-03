import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import PinGrid from '../components/PinGrid';
import CategoryFilters from '../components/CategoryFilters';
import { sponsoredPins as fallbackSponsoredPins } from '../data/sponsoredPins';
import { fetchAdvertisingCampaigns } from '../services/adsApi';
import { mapSponsoredPinToPin } from '../utils/adsMapper';
import { Pin } from '../types/pin';

const AdvertisingCampaignsPage: React.FC = () => {
  const navigate = useNavigate();
  const [activeCategory, setActiveCategory] = useState<string>('All');
  const [campaignPins, setCampaignPins] = useState<Pin[]>(fallbackSponsoredPins);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadCampaigns = async () => {
      setLoading(true);
      setError(null);
      try {
        const campaigns = await fetchAdvertisingCampaigns({ includePins: true });
        const remotePins = campaigns
          .flatMap((campaign) => campaign.featuredPins || [])
          .map(mapSponsoredPinToPin);

        if (remotePins.length === 0) {
          setCampaignPins(fallbackSponsoredPins);
        } else {
          setCampaignPins(remotePins);
        }
      } catch (err) {
        console.error('Unable to load advertising campaigns', err);
        setError('Unable to load advertising campaigns. Showing curated picks instead.');
        setCampaignPins(fallbackSponsoredPins);
      } finally {
        setLoading(false);
      }
    };

    loadCampaigns();
  }, []);

  const categories = useMemo(() => {
    const cats = new Set<string>();
    campaignPins.forEach((pin) => {
      if (pin.sponsorName) {
        cats.add(pin.sponsorName);
      }
    });
    return ['All', ...Array.from(cats)];
  }, [campaignPins]);

  const filteredPins = useMemo(() => {
    if (activeCategory === 'All') {
      return campaignPins;
    }
    return campaignPins.filter((pin) => pin.sponsorName === activeCategory);
  }, [activeCategory, campaignPins]);

  return (
    <main className="container-fluid px-4 px-lg-5 py-4 py-md-5">
      <button
        type="button"
        className="btn btn-outline-secondary btn-sm mb-4 rounded-pill px-3 shadow-sm"
        onClick={() => navigate(-1)}
      >
        ← Back
      </button>

      <div className="mb-5">
        <h1 className="h2 mb-2 fw-bold">⭐ Advertising Campaigns</h1>
        <p className="text-muted lead lh-base">
          Discover curated sponsored content and advertising campaigns from trusted brands
        </p>
      </div>

      <CategoryFilters
        categories={categories}
        activeCategory={activeCategory}
        onSelect={setActiveCategory}
      />

      {loading ? (
        <section className="bg-white rounded-4 rounded-5 shadow-lg p-5 text-center mt-4">
          <p className="text-muted mb-0">Loading campaigns…</p>
        </section>
      ) : filteredPins.length > 0 ? (
        <>
          <div className="d-flex justify-content-between align-items-center mb-4 mt-4">
            <h2 className="h4 mb-0 fw-bold">
              {activeCategory === 'All' 
                ? `All Campaigns (${filteredPins.length})` 
                : `${activeCategory} Campaigns (${filteredPins.length})`}
            </h2>
          </div>
          <PinGrid pins={filteredPins} />
        </>
      ) : (
        <section className="bg-white rounded-4 rounded-5 shadow-lg p-5 text-center mt-4">
          <p className="text-muted mb-0">
            {error || 'No campaigns found for this category.'}
          </p>
        </section>
      )}

      <section className="bg-white rounded-4 rounded-5 shadow-lg p-4 p-md-5 mt-5">
        <h2 className="h5 mb-3 fw-bold">About Sponsored Content</h2>
        <p className="text-muted mb-3">
          Sponsored Pins are clearly marked advertisements from businesses and brands. 
          These campaigns help you discover products, services, and promotions that may 
          interest you based on your preferences and browsing activity.
        </p>
        <p className="text-muted small mb-0">
          All sponsored content is clearly labeled with a "Sponsored" badge to ensure 
          transparency and help you distinguish between regular Pins and advertisements.
        </p>
      </section>
    </main>
  );
};

export default AdvertisingCampaignsPage;

