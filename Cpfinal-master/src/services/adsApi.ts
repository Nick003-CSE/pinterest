import axios from 'axios';
import { API_BASE_URL } from '../config/api';

export interface SponsoredPinResponse {
  id: number;
  pinId: number | null;
  title: string | null;
  description: string | null;
  mediaUrl: string | null;
  sourceUrl: string | null;
  sponsoredLabel: string | null;
  ctaText: string | null;
  ctaUrl: string | null;
  priority: number | null;
  featured: boolean | null;
  businessProfileId: number | null;
  businessName: string | null;
  businessUsername: string | null;
  businessLogoUrl: string | null;
  campaignId: number | null;
  campaignName: string | null;
  campaignObjective: string | null;
  campaignTheme: string | null;
  campaignLandingPageUrl: string | null;
  pinKeywords: string[] | null;
  targetingKeywords: string[] | null;
  createdAt: string | null;
  updatedAt: string | null;
}

export interface AdvertisingCampaignResponse {
  id: number;
  name: string;
  headline: string | null;
  summary: string | null;
  objective: string | null;
  status: string;
  curatedTheme: string | null;
  heroImageUrl: string | null;
  landingPageUrl: string | null;
  audienceFocus: string | null;
  dailyBudget: number | null;
  primaryMetric: string | null;
  startDate: string | null;
  endDate: string | null;
  businessProfileId: number | null;
  businessName: string | null;
  businessUsername: string | null;
  businessLogoUrl: string | null;
  sponsoredPinIds: number[];
  featuredPins?: SponsoredPinResponse[];
}

const adsBase = `${API_BASE_URL}/ads`;

export const fetchSponsoredPins = async (params?: {
  limit?: number;
  interests?: string[];
}): Promise<SponsoredPinResponse[]> => {
  const response = await axios.get<SponsoredPinResponse[]>(`${adsBase}/sponsored-pins`, {
    params: {
      limit: params?.limit,
      interests: params?.interests,
    },
  });
  return Array.isArray(response.data) ? response.data : [];
};

export const fetchAdvertisingCampaigns = async (params?: {
  includePins?: boolean;
  status?: string;
  interests?: string[];
}): Promise<AdvertisingCampaignResponse[]> => {
  const response = await axios.get<AdvertisingCampaignResponse[]>(`${adsBase}/campaigns`, {
    params: {
      includePins: params?.includePins ?? false,
      status: params?.status,
      interests: params?.interests,
    },
  });
  return Array.isArray(response.data) ? response.data : [];
};

