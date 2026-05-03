package com.example.demo.dto.ads;

import com.example.demo.entity.enums.CampaignStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdvertisingCampaignResponse {
    private Long id;
    private String name;
    private String headline;
    private String summary;
    private String objective;
    private CampaignStatus status;
    private String curatedTheme;
    private String heroImageUrl;
    private String landingPageUrl;
    private String audienceFocus;
    private BigDecimal dailyBudget;
    private String primaryMetric;
    private LocalDate startDate;
    private LocalDate endDate;
    private Instant createdAt;
    private Instant updatedAt;
    private Long businessProfileId;
    private String businessName;
    private String businessUsername;
    private String businessLogoUrl;
    private List<Long> sponsoredPinIds;
    private List<SponsoredPinResponse> featuredPins;
}

