import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import axios from 'axios';
import { BusinessProfile, Showcase } from '../types/business';
import { useAuth } from '../context/AuthContext';
import PinGrid from '../components/PinGrid';
import { Pin } from '../types/pin';
import { API_BASE_URL } from '../config/api';

const BusinessDetailPage: React.FC = () => {
  const { businessId } = useParams<{ businessId: string }>();
  const navigate = useNavigate();
  const { backendUserId } = useAuth();
  const [business, setBusiness] = useState<BusinessProfile | null>(null);
  const [showcases, setShowcases] = useState<Showcase[]>([]);
  const [pins, setPins] = useState<Pin[]>([]);
  const [loading, setLoading] = useState(true);
  const [isFollowing, setIsFollowing] = useState(false);

  const mapToShowcase = (data: any): Showcase => ({
    id: String(data.id),
    businessProfileId: String(data.businessProfileId),
    businessName: data.businessName || 'Unknown Business',
    title: data.title || '',
    description: data.description || '',
    theme: data.theme || '',
    coverImageUrl: data.coverImageUrl || 'https://picsum.photos/id/1030/800/600',
    featured: data.featured || false,
    pinCount: data.pinCount || 0,
    pinIds: (data.pinIds || []).map((id: number) => String(id)),
    createdAt: data.createdAt || new Date().toISOString(),
  });

  useEffect(() => {
    const fetchBusinessData = async () => {
      if (!businessId) return;
      setLoading(true);
      try {
        const [businessResponse, showcasesResponse] = await Promise.all([
          axios.get(`${API_BASE_URL}/business/profiles/${businessId}`, {
            params: { userId: backendUserId },
          }),
          axios.get(`${API_BASE_URL}/business/profiles/${businessId}/showcases`),
        ]);

        const businessData = businessResponse.data;
        const mappedBusiness: BusinessProfile = {
          id: String(businessData.id),
          name: businessData.name || 'Unknown Business',
          username: businessData.username || '',
          description: businessData.description || '',
          logoUrl: businessData.logoUrl || `https://ui-avatars.com/api/?background=0f172a&color=fff&name=${encodeURIComponent(businessData.name || 'Business')}`,
          websiteUrl: businessData.websiteUrl || '',
          category: businessData.category || 'General',
          verified: businessData.verified || false,
          followerCount: businessData.followerCount || 0,
          isFollowing: businessData.isFollowing || false,
          createdAt: businessData.createdAt || new Date().toISOString(),
        };

        setBusiness(mappedBusiness);
        setIsFollowing(mappedBusiness.isFollowing);
        setShowcases(showcasesResponse.data.map(mapToShowcase));

        // Fetch pins for showcases
        const allPinIds = showcasesResponse.data.flatMap((s: any) => s.pinIds || []);
        if (allPinIds.length > 0) {
          // Fetch pins from backend (you may need to create an endpoint for this)
          // For now, we'll use empty array
          setPins([]);
        }
      } catch (error) {
        console.error('Failed to load business data:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchBusinessData();
  }, [businessId, backendUserId]);

  const handleFollow = async () => {
    if (!backendUserId || !business) return;
    try {
      if (isFollowing) {
        await axios.post(`${API_BASE_URL}/business/${backendUserId}/unfollow`, {
          businessProfileId: Number(business.id),
        });
        setIsFollowing(false);
        setBusiness({ ...business, isFollowing: false, followerCount: Math.max(0, business.followerCount - 1) });
      } else {
        await axios.post(`${API_BASE_URL}/business/${backendUserId}/follow`, {
          businessProfileId: Number(business.id),
        });
        setIsFollowing(true);
        setBusiness({ ...business, isFollowing: true, followerCount: business.followerCount + 1 });
      }
    } catch (error) {
      console.error('Failed to follow/unfollow business:', error);
      alert('Failed to update follow status');
    }
  };

  if (loading) {
    return (
      <main className="container py-5">
        <div className="text-center py-5 text-muted">Loading business profile...</div>
      </main>
    );
  }

  if (!business) {
    return (
      <main className="container py-5">
        <p>
          We couldn't find that business profile. <Link to="/businesses">Browse businesses.</Link>
        </p>
      </main>
    );
  }

  return (
    <main className="container py-5 business-detail-page">
      <button
        type="button"
        className="btn btn-outline-secondary btn-sm mb-4"
        onClick={() => navigate(-1)}
      >
        ← Back
      </button>

      <section className="bg-white rounded-4 rounded-5 shadow-lg p-4 p-md-5 mb-5">
        <div className="row g-4 align-items-center">
          <div className="col-12 col-md-4">
            <div className="business-logo-container d-flex justify-content-center justify-content-md-start mb-4 mb-md-0">
              <div className="business-logo rounded-4 rounded-5 overflow-hidden shadow-lg" style={{ width: '200px', height: '200px' }}>
                <img
                  src={business.logoUrl}
                  alt={business.name}
                  className="w-100 h-100 object-fit-cover"
                />
              </div>
            </div>
          </div>
          <div className="col-12 col-md-8">
            <div className="d-flex flex-wrap gap-2 mb-3 align-items-center">
              <span className="badge bg-body-secondary text-dark px-3 py-2">
                {business.category}
              </span>
              {business.verified && (
                <span className="badge bg-primary px-3 py-2" title="Verified Business">
                  ✓ Verified
                </span>
              )}
            </div>
            <h1 className="h2 mb-3 fw-bold">{business.name}</h1>
            <p className="lead mb-4 lh-base">{business.description}</p>
            <p className="text-muted mb-4">👥 {business.followerCount.toLocaleString()} followers</p>

            <div className="d-flex flex-column flex-md-row gap-3 mb-4 flex-wrap">
              {business.websiteUrl && (
                <a
                  href={business.websiteUrl}
                  target="_blank"
                  rel="noreferrer"
                  className="btn btn-dark rounded-pill px-4 shadow-sm"
                >
                  🌐 Visit Website →
                </a>
              )}
              <button
                className={`btn rounded-pill px-4 shadow-sm ${isFollowing ? 'btn-outline-secondary' : 'btn-dark'}`}
                onClick={handleFollow}
                disabled={!backendUserId}
              >
                {isFollowing ? '✓ Following' : '+ Follow'}
              </button>
              <button className="btn btn-outline-secondary rounded-pill px-4 shadow-sm">
                📤 Share
              </button>
            </div>
          </div>
        </div>
      </section>

      {showcases.length > 0 && (
        <>
          <div className="d-flex justify-content-between align-items-center mb-4">
            <h2 className="h4 mb-0 fw-bold">Showcases ({showcases.length})</h2>
          </div>
          <div className="row g-4 mb-5">
            {showcases.map((showcase) => (
              <div key={showcase.id} className="col-12 col-md-6 col-lg-4">
                <Link
                  to={`/business/${showcase.businessProfileId}/showcase/${showcase.id}`}
                  className="text-decoration-none text-reset"
                >
                  <div className="card h-100 shadow-sm border-0 rounded-4 overflow-hidden">
                    <div className="position-relative" style={{ height: '200px', overflow: 'hidden' }}>
                      <img
                        src={showcase.coverImageUrl}
                        alt={showcase.title}
                        className="w-100 h-100"
                        style={{ objectFit: 'cover' }}
                      />
                      {showcase.featured && (
                        <span className="position-absolute top-0 end-0 m-2 badge bg-warning text-dark">
                          Featured
                        </span>
                      )}
                    </div>
                    <div className="card-body p-4">
                      <h3 className="h5 mb-2 fw-bold">{showcase.title}</h3>
                      <p className="small text-muted mb-2">{showcase.description}</p>
                      <div className="d-flex justify-content-between align-items-center">
                        <span className="badge bg-light text-dark">{showcase.theme}</span>
                        <span className="text-muted small">📌 {showcase.pinCount} pins</span>
                      </div>
                    </div>
                  </div>
                </Link>
              </div>
            ))}
          </div>
        </>
      )}

      {pins.length > 0 ? (
        <>
          <div className="d-flex justify-content-between align-items-center mb-4">
            <h2 className="h4 mb-0 fw-bold">Featured Pins ({pins.length})</h2>
          </div>
          <PinGrid pins={pins} />
        </>
      ) : showcases.length === 0 && (
        <section className="bg-white rounded-4 rounded-5 shadow-lg p-5 text-center">
          <p className="text-muted mb-0">This business doesn't have any showcases or featured pins yet.</p>
        </section>
      )}
    </main>
  );
};

export default BusinessDetailPage;

