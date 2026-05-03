import React from 'react';

interface HeroBannerProps {
  highlights: string[];
}

const HeroBanner: React.FC<HeroBannerProps> = ({ highlights }) => (
  <section className="hero-banner rounded-4 rounded-5 text-white mb-4 overflow-hidden shadow-lg">
    <div className="row g-0">
      <div className="col-12 col-lg-7 p-4 p-md-5 p-lg-5 d-flex flex-column justify-content-center">
        <p className="text-uppercase small mb-2 opacity-75 fw-semibold letter-spacing-1">✨ Winter reset</p>
        <h1 className="display-5 display-md-4 fw-bold mb-3 lh-sm">Design a calmer start to 2026</h1>
        <p className="lead mb-4 pe-lg-5 fs-5 lh-base">
          Build boards for slow mornings, tactile workspaces, and mindful hosting with curated
          palettes and space-saving layouts inspired by Framescape trends.
        </p>
        <div className="d-flex flex-wrap gap-2 mt-auto">
          {highlights.map((item) => (
            <span key={item} className="badge bg-white bg-opacity-90 text-dark px-3 py-2 rounded-pill shadow-sm fw-medium">
              {item}
            </span>
          ))}
        </div>
      </div>
      <div className="col-lg-5 d-none d-lg-block hero-media position-relative">
        <div className="hero-photo" />
      </div>
    </div>
  </section>
);

export default HeroBanner;

