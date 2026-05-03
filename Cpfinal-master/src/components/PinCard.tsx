import React from 'react';
import { Pin } from '../types/pin';
import { useSavedPins } from '../context/SavedPinsContext';

interface PinCardProps {
  pin: Pin;
}

const PinCard: React.FC<PinCardProps> = ({ pin }) => {
  const { isPinSaved, savePin, unsavePin } = useSavedPins();
  const isSaved = isPinSaved(pin.id);

  const handleSave = (event: React.MouseEvent) => {
    event.preventDefault();
    event.stopPropagation();
    if (isSaved) {
      unsavePin(pin.id);
    } else {
      savePin(pin);
    }
  };

  return (
    <article className="pin-card card border-0 shadow-sm rounded-3 overflow-hidden position-relative h-100">
      <div className="pin-photo position-relative">
        <img src={pin.imageUrl} alt={pin.title} className="w-100 h-100 object-fit-cover" />
        {pin.sponsored && (
          <span className="badge bg-warning text-dark position-absolute top-0 start-0 m-1 shadow-sm" style={{ zIndex: 10, fontSize: '0.7rem', padding: '0.25rem 0.5rem' }}>
            <span className="me-1">⭐</span> Sponsored
          </span>
        )}
        <button
          type="button"
          className={`btn btn-sm rounded-pill save-btn shadow-sm ${isSaved ? 'btn-dark' : 'btn-light'}`}
          onClick={handleSave}
          aria-label={isSaved ? 'Unsave pin' : 'Save pin'}
          style={{ fontSize: '0.75rem', padding: '0.25rem 0.75rem' }}
        >
          {isSaved ? '✓ Saved' : '💾 Save'}
        </button>
      </div>
      <div className="card-body p-2 p-md-3 d-flex flex-column">
        <div className="mb-1">
          <span className="badge bg-body-secondary text-dark px-2 py-1" style={{ fontSize: '0.7rem' }}>{pin.category}</span>
        </div>
        <h3 className="h6 fw-semibold mb-1" style={{ fontSize: '0.9rem' }}>{pin.title}</h3>
        <p className="text-muted small mb-2 lh-sm flex-grow-1" style={{ fontSize: '0.8rem' }}>{pin.description}</p>
        {pin.palette && (
          <div className="d-flex gap-1 mb-2 flex-wrap">
            {pin.palette.map((color) => (
              <span
                key={`${pin.id}-${color}`}
                className="palette-dot rounded-pill shadow-sm"
                style={{ backgroundColor: color, width: '24px', height: '12px' }}
                aria-label={`${color} swatch`}
                title={color}
              />
            ))}
          </div>
        )}
        <div className="d-flex align-items-center justify-content-between pt-2 border-top mt-auto">
          <div>
            <p className="mb-0 fw-semibold" style={{ fontSize: '0.75rem' }}>{pin.author.name}</p>
            {pin.author.location && <small className="text-muted d-block" style={{ fontSize: '0.7rem' }}>{pin.author.location}</small>}
          </div>
          <div className="text-end">
            <div className="text-muted" style={{ fontSize: '0.7rem' }}>
              <span className="fw-semibold">{pin.stats.saves.toLocaleString()}</span> saves
            </div>
            <div className="text-muted" style={{ fontSize: '0.7rem' }}>
              <span className="fw-semibold">{pin.stats.shares.toLocaleString()}</span> shares
            </div>
          </div>
        </div>
      </div>
    </article>
  );
};

export default PinCard;

