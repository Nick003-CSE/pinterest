package com.example.demo.dto.ads;

import java.time.Instant;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SponsoredPinResponse {
    private Long id;
    private Long pinId;
    private String title;
    private String description;
    private String mediaUrl;
    private String sourceUrl;
    private String sponsoredLabel;
    private String ctaText;
    private String ctaUrl;
    private Integer priority;
    private Boolean featured;
    private Long businessProfileId;
    private String businessName;
    private String businessUsername;
    private String businessLogoUrl;
    private Long campaignId;
    private String campaignName;
    private String campaignObjective;
    private String campaignTheme;
    private String campaignLandingPageUrl;
    private Set<String> pinKeywords;
    private Set<String> targetingKeywords;
    private Instant createdAt;
    private Instant updatedAt;
}

