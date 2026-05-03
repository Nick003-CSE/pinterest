import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import axios from 'axios';
import { BusinessProfile, Showcase } from '../types/business';
import { useAuth } from '../context/AuthContext';
import { API_BASE_URL } from '../config/api';

const BusinessProfilesPage: React.FC = () => {
  const navigate = useNavigate();
  const { backendUserId } = useAuth();
  const [businessProfiles, setBusinessProfiles] = useState<BusinessProfile[]>([]);
  const [showcases, setShowcases] = useState<Showcase[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string>('All');
  const [viewMode, setViewMode] = useState<'profiles' | 'showcases'>('profiles');

  const mapToBusinessProfile = (data: any): BusinessProfile => ({
    id: String(data.id),
    name: data.name || 'Unknown Business',
    username: data.username || '',
    description: data.description || '',
    logoUrl: data.logoUrl || `https://ui-avatars.com/api/?background=0f172a&color=fff&name=${encodeURIComponent(data.name || 'Business')}`,
    websiteUrl: data.websiteUrl || '',
    category: data.category || 'General',
    verified: data.verified || false,
    followerCount: data.followerCount || 0,
    isFollowing: data.isFollowing || false,
    createdAt: data.createdAt || new Date().toISOString(),
  });

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
    const fetchData = async () => {
      setLoading(true);
      try {
        if (viewMode === 'profiles') {
          const response = await axios.get(`${API_BASE_URL}/business/profiles`);
          const data = Array.isArray(response.data) ? response.data : [];
          setBusinessProfiles(data.map(mapToBusinessProfile));
        } else {
          const response = await axios.get(`${API_BASE_URL}/business/showcases/featured`);
          const data = Array.isArray(response.data) ? response.data : [];
          setShowcases(data.map(mapToShowcase));
        }
      } catch (error) {
        console.error('Failed to load business data:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [viewMode]);

  const handleFollowBusiness = async (businessId: string, isFollowing: boolean) => {
    if (!backendUserId) {
      alert('Please sign in to follow businesses');
      return;
    }
    try {
      if (isFollowing) {
        await axios.post(`${API_BASE_URL}/business/${backendUserId}/unfollow`, {
          businessProfileId: Number(businessId),
        });
      } else {
        await axios.post(`${API_BASE_URL}/business/${backendUserId}/follow`, {
          businessProfileId: Number(businessId),
        });
      }
      // Refresh the list
      const response = await axios.get(`${API_BASE_URL}/business/profiles`);
      setBusinessProfiles(response.data.map(mapToBusinessProfile));
    } catch (error) {
      console.error('Failed to follow/unfollow business:', error);
      alert('Failed to update follow status');
    }
  };

  const filteredBusinesses = useMemo(() => {
    let filtered = [...businessProfiles];
    
    if (searchTerm) {
      const lower = searchTerm.toLowerCase();
      filtered = filtered.filter(
        (business) =>
          business.name.toLowerCase().includes(lower) ||
          business.description.toLowerCase().includes(lower) ||
          business.category.toLowerCase().includes(lower)
      );
    }
    
    if (selectedCategory !== 'All') {
      filtered = filtered.filter((business) => business.category === selectedCategory);
    }
    
    return filtered;
  }, [businessProfiles, searchTerm, selectedCategory]);

  const categories = useMemo(() => {
    const cats = new Set(businessProfiles.map((b) => b.category).filter(Boolean));
    return Array.from(cats).sort();
  }, [businessProfiles]);

  return (
    <main className="container py-4 py-md-5">
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4 mb-md-5">
        <div>
          <h1 className="h2 mb-1 fw-bold">🏢 Business Profiles & Showcases</h1>
          <p className="text-muted small mb-0">Discover brands, products, and curated collections</p>
        </div>
        <div className="btn-group" role="group">
          <button
            type="button"
            className={`btn ${viewMode === 'profiles' ? 'btn-dark' : 'btn-outline-dark'}`}
            onClick={() => setViewMode('profiles')}
          >
            Profiles
          </button>
          <button
            type="button"
            className={`btn ${viewMode === 'showcases' ? 'btn-dark' : 'btn-outline-dark'}`}
            onClick={() => setViewMode('showcases')}
          >
            Showcases
          </button>
        </div>
      </div>

      {viewMode === 'profiles' && (
        <>
          <div className="d-flex flex-column flex-md-row gap-3 mb-4">
            <input
              type="search"
              className="form-control flex-grow-1"
              placeholder="🔍 Search businesses by name, description, or category..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
            <select
              className="form-select w-auto"
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
            >
              <option value="All">All Categories</option>
              {categories.map((cat) => (
                <option key={cat} value={cat}>
                  {cat}
                </option>
              ))}
            </select>
          </div>

          {loading ? (
            <div className="text-center py-5 text-muted">Loading businesses...</div>
          ) : filteredBusinesses.length > 0 ? (
            <div className="row g-4">
              {filteredBusinesses.map((business) => (
                <div key={business.id} className="col-12 col-md-6 col-lg-4">
                  <div className="card h-100 shadow-sm border-0 rounded-4">
                    <div className="card-body p-4">
                      <div className="d-flex align-items-start gap-3 mb-3">
                        <img
                          src={business.logoUrl}
                          alt={business.name}
                          className="rounded-circle"
                          style={{ width: '64px', height: '64px', objectFit: 'cover' }}
                        />
                        <div className="flex-grow-1">
                          <div className="d-flex align-items-center gap-2 mb-1">
                            <h3 className="h5 mb-0 fw-bold">{business.name}</h3>
                            {business.verified && (
                              <span className="badge bg-primary rounded-pill" title="Verified Business">
                                ✓
                              </span>
                            )}
                          </div>
                          <p className="text-muted small mb-0">@{business.username}</p>
                          <p className="text-muted small mb-0">{business.category}</p>
                        </div>
                      </div>
                      <p className="small mb-3">{business.description}</p>
                      {business.websiteUrl && (
                        <a
                          href={business.websiteUrl}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="small text-decoration-none d-block mb-3"
                        >
                          🌐 {business.websiteUrl}
                        </a>
                      )}
                      <div className="d-flex justify-content-between align-items-center">
                        <span className="text-muted small">
                          👥 {business.followerCount.toLocaleString()} followers
                        </span>
                        <button
                          className={`btn btn-sm rounded-pill ${
                            business.isFollowing ? 'btn-outline-secondary' : 'btn-dark'
                          }`}
                          onClick={() => handleFollowBusiness(business.id, business.isFollowing)}
                        >
                          {business.isFollowing ? '✓ Following' : '+ Follow'}
                        </button>
                      </div>
                      <Link
                        to={`/business/${business.id}`}
                        className="btn btn-outline-dark btn-sm rounded-pill w-100 mt-3"
                      >
                        View Profile →
                      </Link>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-5">
              <p className="text-muted">
                {searchTerm || selectedCategory !== 'All'
                  ? 'No businesses found matching your search'
                  : 'No business profiles available'}
              </p>
            </div>
          )}
        </>
      )}

      {viewMode === 'showcases' && (
        <>
          <div className="mb-4">
            <input
              type="search"
              className="form-control"
              placeholder="🔍 Search showcases..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>

          {loading ? (
            <div className="text-center py-5 text-muted">Loading showcases...</div>
          ) : showcases.length > 0 ? (
            <div className="row g-4">
              {showcases
                .filter((showcase) =>
                  searchTerm
                    ? showcase.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
                      showcase.businessName.toLowerCase().includes(searchTerm.toLowerCase())
                    : true
                )
                .map((showcase) => (
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
                          <p className="text-muted small mb-1">{showcase.businessName}</p>
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
          ) : (
            <div className="text-center py-5">
              <p className="text-muted">No showcases available</p>
            </div>
          )}
        </>
      )}
    </main>
  );
};

export default BusinessProfilesPage;

