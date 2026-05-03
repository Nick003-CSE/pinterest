import { Pin } from '../types/pin';
import { SponsoredPinResponse } from '../services/adsApi';

const fallbackStats = { saves: 0, shares: 0, likes: 0 };

export const mapSponsoredPinToPin = (pin: SponsoredPinResponse): Pin => {
  const imageUrl = pin.mediaUrl || undefined;
  const sponsorName = pin.businessName || pin.sponsoredLabel || 'Sponsored';
  const website = pin.ctaUrl || pin.campaignLandingPageUrl || undefined;

  return {
    id: String(pin.pinId ?? pin.id),
    title: pin.title || pin.campaignName || 'Sponsored Pin',
    description: pin.description || '',
    category: pin.campaignTheme || 'Sponsored',
    imageUrl,
    author: {
      name: sponsorName,
    },
    stats: fallbackStats,
    keywords: pin.targetingKeywords || pin.pinKeywords || undefined,
    sponsored: true,
    sponsorName,
    sponsorWebsite: website,
    createdAt: pin.createdAt || new Date().toISOString(),
    attribution: pin.sponsoredLabel || undefined,
  };
};

