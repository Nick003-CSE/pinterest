import React from 'react';
import { Link } from 'react-router-dom';
import { Pin } from '../types/pin';
import { useSavedPins } from '../context/SavedPinsContext';

interface SponsoredRailProps {
  pins: Pin[];
}

const SponsoredRail: React.FC<SponsoredRailProps> = ({ pins }) => {
  const { isPinSaved, savePin, unsavePin } = useSavedPins();

  if (!pins || pins.length === 0) {
    return null;
  }

  return (
    <section className="sponsored-rail bg-white rounded-4 rounded-5 shadow-sm p-4 p-md-5 my-4 my-md-5">
    <div className="d-flex justify-content-between align-items-center mb-4">
      <div>
        <p className="text-uppercase small text-muted mb-1 fw-semibold letter-spacing-1">⭐ Sponsored</p>
        <h2 className="h5 mb-0 fw-bold">Campaigns picked for you</h2>
      </div>
      <Link to="/campaigns" className="btn btn-outline-secondary btn-sm rounded-pill px-3 shadow-sm">
        View all →
      </Link>
    </div>
    <div className="row g-3">
      {pins.map((pin) => (
        <div key={pin.id} className="col-12 col-md-6">
          <Link
            to={`/pin/${pin.id}`}
            className="text-decoration-none text-reset"
          >
            <article className="sponsored-card card border-0 rounded-4">
              <div className="sponsored-image">
                <img src={pin.imageUrl} alt={pin.title} className="w-100 h-100 object-fit-cover" />
                <span className="badge bg-warning text-dark">Sponsored</span>
              </div>
              <div className="card-body p-3 p-md-4">
                <p className="text-muted small mb-1 fw-medium">{pin.sponsorName}</p>
                <h3 className="h6 fw-semibold mb-2">{pin.title}</h3>
                <p className="text-muted small mb-3 lh-sm">{pin.description}</p>
                <div className="d-flex gap-2 flex-wrap">
                  {pin.sponsorWebsite && (
                    <a
                      className="btn btn-dark btn-sm rounded-pill px-3 shadow-sm"
                      href={pin.sponsorWebsite}
                      target="_blank"
                      rel="noopener noreferrer"
                      onClick={(e) => {
                        e.stopPropagation();
                      }}
                    >
                      Learn more
                    </a>
                  )}
                  <button
                    className={`btn btn-sm rounded-pill px-3 ${isPinSaved(pin.id) ? 'btn-dark shadow-sm' : 'btn-outline-secondary'}`}
                    onClick={(e) => {
                      e.preventDefault();
                      e.stopPropagation();
                      if (isPinSaved(pin.id)) {
                        unsavePin(pin.id);
                      } else {
                        savePin(pin);
                      }
                    }}
                  >
                    {isPinSaved(pin.id) ? '✓ Saved' : '💾 Save'}
                  </button>
                </div>
              </div>
            </article>
          </Link>
        </div>
      ))}
    </div>
  </section>
  );
};

export default SponsoredRail;

