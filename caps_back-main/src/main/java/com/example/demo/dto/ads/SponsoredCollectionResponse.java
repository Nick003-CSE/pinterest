package com.example.demo.dto.ads;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SponsoredCollectionResponse {
    private String theme;
    private String description;
    private List<AdvertisingCampaignResponse> campaigns;
    private List<SponsoredPinResponse> highlightedPins;
}

