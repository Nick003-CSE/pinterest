import React from 'react';
import { SpotlightCollection } from '../data/spotlights';

interface SpotlightPanelProps {
  collections: SpotlightCollection[];
}

const SpotlightPanel: React.FC<SpotlightPanelProps> = ({ collections }) => (
  <aside className="spotlight-panel bg-white rounded-4 rounded-5 shadow-sm p-4 p-md-4">
    <div className="mb-4">
      <p className="text-uppercase small text-muted mb-2 fw-semibold letter-spacing-1">✨ Curated picks</p>
      <h2 className="h5 fw-bold mb-0">Spotlight boards</h2>
    </div>
    <div className="d-flex flex-column gap-3">
      {collections.map((collection) => (
        <article key={collection.id} className="spotlight-card d-flex gap-3 align-items-center p-3 rounded-3 bg-light bg-opacity-50 transition-all">
          <div
            className="spotlight-thumbnail rounded-4 flex-shrink-0 shadow-sm"
            style={{ background: collection.gradient }}
          >
            <img
              src={collection.coverUrl}
              alt={collection.title}
              className="w-100 h-100 object-fit-cover rounded-4"
            />
          </div>
          <div className="flex-grow-1">
            <p className="fw-semibold mb-1 small">{collection.title}</p>
            <small className="text-muted d-block lh-sm">{collection.description}</small>
          </div>
        </article>
      ))}
    </div>
  </aside>
);

export default SpotlightPanel;

