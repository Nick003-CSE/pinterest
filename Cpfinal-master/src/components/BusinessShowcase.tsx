import React from 'react';
import { Link } from 'react-router-dom';
import { BusinessProfile } from '../types/business';

interface BusinessShowcaseProps {
  businesses: BusinessProfile[];
}

const BusinessShowcase: React.FC<BusinessShowcaseProps> = ({ businesses }) => (
  <section className="business-showcase bg-white rounded-4 rounded-5 shadow-sm p-4 p-md-4 mt-4">
    <div className="mb-4">
      <p className="text-uppercase small text-muted mb-2 fw-semibold letter-spacing-1">🏢 Business profiles</p>
      <h2 className="h6 mb-0 fw-bold">Discover brand showcases</h2>
    </div>
    <div className="d-flex flex-column gap-3">
      {businesses.map((biz) => (
        <Link
          key={biz.id}
          to={`/business/${biz.id}`}
          className="text-decoration-none text-reset"
        >
          <article className="business-card d-flex gap-3 p-3 rounded-3 bg-light bg-opacity-50 transition-all">
            <div className="business-cover rounded-4 flex-shrink-0 shadow-sm overflow-hidden">
              <img src={biz.logoUrl} alt={biz.name} className="w-100 h-100 object-fit-cover" />
            </div>
            <div className="flex-grow-1">
              <div className="d-flex align-items-center gap-2 mb-1">
                <p className="fw-semibold mb-0 small">{biz.name}</p>
                {biz.verified && (
                  <span className="badge bg-primary rounded-pill" title="Verified Business">
                    ✓
                  </span>
                )}
              </div>
              <small className="text-muted d-block mb-2 lh-sm">{biz.description}</small>
              <div className="d-flex flex-wrap gap-2 mb-2">
                <span className="badge bg-body-secondary text-dark px-2 py-1">
                  {biz.category}
                </span>
              </div>
              <div className="d-flex justify-content-between align-items-center">
                <span className="small text-muted">👥 {biz.followerCount.toLocaleString()} followers</span>
                <span className="small text-primary fw-medium">View profile →</span>
              </div>
            </div>
          </article>
        </Link>
      ))}
    </div>
  </section>
);

export default BusinessShowcase;

