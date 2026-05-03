import React from 'react';
import { Link } from 'react-router-dom';
import { Pin } from '../types/pin';
import PinCard from './PinCard';

interface PinGridProps {
  pins: Pin[];
}

const PinGrid: React.FC<PinGridProps> = ({ pins }) => (
  <section className="pin-grid-container">
    <div className="pin-grid">
      {pins.map((pin) => (
        <div key={pin.id} className="pin-grid-item">
          <Link to={`/pin/${pin.id}`} className="text-decoration-none text-reset d-block h-100">
            <PinCard pin={pin} />
          </Link>
        </div>
      ))}
    </div>
  </section>
);

export default PinGrid;

