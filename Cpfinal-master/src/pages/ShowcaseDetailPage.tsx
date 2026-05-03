import React, { useEffect, useState, useMemo } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import axios from 'axios';
import { Showcase } from '../types/business';
import { Pin } from '../types/pin';
import { API_BASE_URL } from '../config/api';
import PinGrid from '../components/PinGrid';

const ShowcaseDetailPage: React.FC = () => {
  const { businessId, showcaseId } = useParams<{ businessId?: string; showcaseId?: string }>();
  const navigate = useNavigate();
  const [showcase, setShowcase] = useState<Showcase | null>(null);
  const [pins, setPins] = useState<Pin[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const mapToShowcase = (data: any): Showcase => ({
    id: String(data.id),
    businessProfileId: String(data.businessProfileId),
    businessName: data.businessName || 'Unknown Business',
    title: data.title || '',
    description: data.description || '',
    theme: data.theme || '',
    coverImageUrl:
      data.coverImageUrl ||
      'https://picsum.photos/id/1030/800/600',
    featured: data.featured || false,
    pinCount: data.pinCount || 0,
    pinIds: (data.pinIds || []).map((id: number) => String(id)),
    createdAt: data.createdAt || new Date().toISOString(),
  });

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
    const fetchShowcase = async () => {
      if (!showcaseId) return;
      setLoading(true);
      setError(null);
      try {
        // Load showcase metadata
        const response = await axios.get(
          `${API_BASE_URL}/business/showcases/${showcaseId}`
        );
        const data = response.data;
        const mappedShowcase = mapToShowcase(data);
        setShowcase(mappedShowcase);

        // Load pins for this showcase
        if (mappedShowcase.pinIds.length > 0) {
          const pinResponses = await Promise.all(
            mappedShowcase.pinIds.map((id) =>
              axios
                .get(`${API_BASE_URL}/pins/${id}`)
                .then((res) => mapPinResponse(res.data))
                .catch(() => null)
            )
          );
          setPins(pinResponses.filter((p): p is Pin => p !== null));
        } else {
          setPins([]);
        }
      } catch (err) {
        console.error('Failed to load showcase', err);
        setError('We could not load this showcase. Please try again later.');
        setShowcase(null);
        setPins([]);
      } finally {
        setLoading(false);
      }
    };

    fetchShowcase();
  }, [showcaseId]);

  const title = useMemo(() => {
    if (!showcase) return 'Showcase';
    return showcase.title || showcase.theme || 'Showcase';
  }, [showcase]);

  if (loading) {
    return (
      <main className="container py-5">
        <div className="text-center py-5 text-muted">Loading showcase…</div>
      </main>
    );
  }

  if (error || !showcase) {
    return (
      <main className="container py-5">
        <p className="text-muted mb-3">{error || 'Showcase not found.'}</p>
        {businessId && (
          <button
            type="button"
            className="btn btn-outline-secondary rounded-pill"
            onClick={() => navigate(`/business/${businessId}`)}
          >
            ← Back to business profile
          </button>
        )}
      </main>
    );
  }

  return (
    <main className="container py-5">
      <button
        type="button"
        className="btn btn-outline-secondary btn-sm mb-4 rounded-pill px-3 shadow-sm"
        onClick={() => navigate(-1)}
      >
        ← Back
      </button>

      <section className="bg-white rounded-4 rounded-5 shadow-lg p-4 p-md-5 mb-5">
        <div className="row g-4 align-items-center">
          <div className="col-12 col-md-6">
            <div className="rounded-4 overflow-hidden shadow-sm" style={{ maxHeight: 320 }}>
              <img
                src={showcase.coverImageUrl}
                alt={showcase.title}
                className="w-100 h-100"
                style={{ objectFit: 'cover' }}
              />
            </div>
          </div>
          <div className="col-12 col-md-6">
            <p className="text-uppercase small text-muted mb-2 fw-semibold">
              Showcase • {showcase.theme || 'Curated collection'}
            </p>
            <h1 className="h3 mb-3 fw-bold">{title}</h1>
            {showcase.description && (
              <p className="text-muted mb-3">{showcase.description}</p>
            )}
            <p className="small text-muted mb-1">
              📌 {showcase.pinCount} pins • Created{' '}
              {new Date(showcase.createdAt).toLocaleDateString()}
            </p>
            {showcase.businessProfileId && (
              <Link
                to={`/business/${showcase.businessProfileId}`}
                className="small text-decoration-none"
              >
                View all from {showcase.businessName}
              </Link>
            )}
          </div>
        </div>
      </section>

      {pins.length > 0 ? (
        <>
          <div className="d-flex justify-content-between align-items-center mb-4">
            <h2 className="h5 mb-0 fw-bold">Pins in this showcase</h2>
          </div>
          <PinGrid pins={pins} />
        </>
      ) : (
        <section className="bg-white rounded-4 rounded-5 shadow-sm p-5 text-center">
          <p className="text-muted mb-0">
            This showcase doesn&apos;t have any pins yet. Check back soon!
          </p>
        </section>
      )}
    </main>
  );
};

export default ShowcaseDetailPage;


