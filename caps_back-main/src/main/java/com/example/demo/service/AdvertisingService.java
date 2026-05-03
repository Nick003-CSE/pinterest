package com.example.demo.service;

import com.example.demo.dto.ads.AdvertisingCampaignResponse;
import com.example.demo.dto.ads.SponsoredCollectionResponse;
import com.example.demo.dto.ads.SponsoredPinResponse;
import com.example.demo.entity.AdvertisingCampaign;
import com.example.demo.entity.BusinessProfile;
import com.example.demo.entity.Pin;
import com.example.demo.entity.SponsoredPin;
import com.example.demo.entity.enums.CampaignStatus;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.AdvertisingCampaignRepository;
import com.example.demo.repository.SponsoredPinRepository;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class AdvertisingService {

    private static final int DEFAULT_SPONSORED_PIN_LIMIT = 12;
    private static final int MAX_SPONSORED_PIN_LIMIT = 50;
    private static final Comparator<SponsoredPin> PRIORITY_COMPARATOR =
            Comparator.comparing(pin -> pin.getPriority() != null ? pin.getPriority() : Integer.MAX_VALUE);

    private final SponsoredPinRepository sponsoredPinRepository;
    private final AdvertisingCampaignRepository advertisingCampaignRepository;

    public AdvertisingService(
            SponsoredPinRepository sponsoredPinRepository,
            AdvertisingCampaignRepository advertisingCampaignRepository) {
        this.sponsoredPinRepository = sponsoredPinRepository;
        this.advertisingCampaignRepository = advertisingCampaignRepository;
    }

    @Transactional(readOnly = true)
    public List<SponsoredPinResponse> getSponsoredPins(List<String> rawInterests, Integer limit) {
        int effectiveLimit = resolveLimit(limit);
        Set<String> normalizedInterests = normalizeInterests(rawInterests);

        Stream<SponsoredPin> stream = sponsoredPinRepository.findAllByOrderByPriorityAsc().stream();
        if (!normalizedInterests.isEmpty()) {
            stream = stream.filter(pin -> matchesInterests(pin, normalizedInterests));
        }

        return stream.limit(effectiveLimit).map(this::mapSponsoredPinResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SponsoredPinResponse getSponsoredPin(Long id) {
        SponsoredPin pin = sponsoredPinRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sponsored pin not found"));
        return mapSponsoredPinResponse(pin);
    }

    @Transactional(readOnly = true)
    public List<SponsoredPinResponse> getSponsoredPinsForCampaign(Long campaignId) {
        advertisingCampaignRepository
                .findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
        return sponsoredPinRepository.findByCampaignIdOrderByPriorityAsc(campaignId).stream()
                .map(this::mapSponsoredPinResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdvertisingCampaignResponse> getCampaigns(
            List<String> interests, CampaignStatus statusFilter, boolean includePins) {
        List<AdvertisingCampaign> campaigns =
                statusFilter != null
                        ? advertisingCampaignRepository.findByStatusOrderByStartDateDesc(statusFilter)
                        : advertisingCampaignRepository.findAllByOrderByStartDateDesc();

        Set<String> normalizedInterests = normalizeInterests(interests);
        campaigns = filterCampaignsByInterest(campaigns, normalizedInterests);

        return campaigns.stream()
                .map(campaign -> mapCampaignResponse(campaign, includePins))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AdvertisingCampaignResponse getCampaign(Long id, boolean includePins) {
        AdvertisingCampaign campaign = advertisingCampaignRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
        return mapCampaignResponse(campaign, includePins);
    }

    @Transactional(readOnly = true)
    public List<SponsoredCollectionResponse> getCuratedCollections(List<String> interests) {
        List<AdvertisingCampaign> campaigns =
                advertisingCampaignRepository.findAllByOrderByStartDateDesc();
        Set<String> normalizedInterests = normalizeInterests(interests);
        campaigns = filterCampaignsByInterest(campaigns, normalizedInterests).stream()
                .filter(c -> StringUtils.hasText(c.getCuratedTheme()))
                .collect(Collectors.toList());

        return campaigns.stream()
                .collect(Collectors.groupingBy(AdvertisingCampaign::getCuratedTheme))
                .entrySet()
                .stream()
                .map(entry -> buildCollection(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(SponsoredCollectionResponse::getTheme, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    private SponsoredCollectionResponse buildCollection(String theme, List<AdvertisingCampaign> campaigns) {
        List<AdvertisingCampaignResponse> campaignResponses = campaigns.stream()
                .map(campaign -> mapCampaignResponse(campaign, false))
                .collect(Collectors.toList());

        List<SponsoredPinResponse> highlightedPins = campaigns.stream()
                .flatMap(campaign -> safeStream(campaign.getSponsoredPins()))
                .sorted(PRIORITY_COMPARATOR)
                .limit(6)
                .map(this::mapSponsoredPinResponse)
                .collect(Collectors.toList());

        String description = campaigns.stream()
                .map(AdvertisingCampaign::getSummary)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse("Curated picks from our advertising partners.");

        return SponsoredCollectionResponse.builder()
                .theme(theme)
                .description(description)
                .campaigns(campaignResponses)
                .highlightedPins(highlightedPins)
                .build();
    }

    private AdvertisingCampaignResponse mapCampaignResponse(AdvertisingCampaign campaign, boolean includePins) {
        BusinessProfile business = campaign.getBusinessProfile();
        List<Long> pinIds = safeStream(campaign.getSponsoredPins())
                .map(SponsoredPin::getId)
                .collect(Collectors.toList());

        List<SponsoredPinResponse> featuredPins = includePins
                ? safeStream(campaign.getSponsoredPins())
                        .sorted(PRIORITY_COMPARATOR)
                        .limit(4)
                        .map(this::mapSponsoredPinResponse)
                        .collect(Collectors.toList())
                : Collections.emptyList();

        return AdvertisingCampaignResponse.builder()
                .id(campaign.getId())
                .name(campaign.getName())
                .headline(campaign.getHeadline())
                .summary(campaign.getSummary())
                .objective(campaign.getObjective())
                .status(campaign.getStatus())
                .curatedTheme(campaign.getCuratedTheme())
                .heroImageUrl(campaign.getHeroImageUrl())
                .landingPageUrl(campaign.getLandingPageUrl())
                .audienceFocus(campaign.getAudienceFocus())
                .dailyBudget(campaign.getDailyBudget())
                .primaryMetric(campaign.getPrimaryMetric())
                .startDate(campaign.getStartDate())
                .endDate(campaign.getEndDate())
                .createdAt(campaign.getCreatedAt())
                .updatedAt(campaign.getUpdatedAt())
                .businessProfileId(business != null ? business.getId() : null)
                .businessName(business != null ? business.getName() : null)
                .businessUsername(business != null ? business.getUsername() : null)
                .businessLogoUrl(business != null ? business.getLogoUrl() : null)
                .sponsoredPinIds(pinIds)
                .featuredPins(featuredPins)
                .build();
    }

    private SponsoredPinResponse mapSponsoredPinResponse(SponsoredPin sponsoredPin) {
        Pin pin = sponsoredPin.getPin();
        AdvertisingCampaign campaign = sponsoredPin.getCampaign();
        BusinessProfile business = sponsoredPin.getBusinessProfile();

        return SponsoredPinResponse.builder()
                .id(sponsoredPin.getId())
                .pinId(pin != null ? pin.getId() : null)
                .title(pin != null ? pin.getTitle() : null)
                .description(pin != null ? pin.getDescription() : null)
                .mediaUrl(pin != null ? pin.getMediaUrl() : null)
                .sourceUrl(pin != null ? pin.getSourceUrl() : null)
                .sponsoredLabel(sponsoredPin.getSponsoredLabel())
                .ctaText(sponsoredPin.getCtaText())
                .ctaUrl(sponsoredPin.getCtaUrl())
                .priority(sponsoredPin.getPriority())
                .featured(sponsoredPin.getFeatured())
                .businessProfileId(business != null ? business.getId() : null)
                .businessName(business != null ? business.getName() : null)
                .businessUsername(business != null ? business.getUsername() : null)
                .businessLogoUrl(business != null ? business.getLogoUrl() : null)
                .campaignId(campaign != null ? campaign.getId() : null)
                .campaignName(campaign != null ? campaign.getName() : null)
                .campaignObjective(campaign != null ? campaign.getObjective() : null)
                .campaignTheme(campaign != null ? campaign.getCuratedTheme() : null)
                .campaignLandingPageUrl(campaign != null ? campaign.getLandingPageUrl() : null)
                .pinKeywords(safeCopy(pin != null ? pin.getKeywords() : Collections.emptySet()))
                .targetingKeywords(safeCopy(sponsoredPin.getTargetKeywords()))
                .createdAt(sponsoredPin.getCreatedAt())
                .updatedAt(sponsoredPin.getUpdatedAt())
                .build();
    }

    private List<AdvertisingCampaign> filterCampaignsByInterest(
            List<AdvertisingCampaign> campaigns, Set<String> normalizedInterests) {
        if (normalizedInterests.isEmpty()) {
            return campaigns;
        }
        return campaigns.stream()
                .filter(campaign -> matchesCampaignInterests(campaign, normalizedInterests))
                .collect(Collectors.toList());
    }

    private boolean matchesCampaignInterests(AdvertisingCampaign campaign, Set<String> interests) {
        if (interests.isEmpty()) {
            return true;
        }

        String theme = normalizeText(campaign.getCuratedTheme());
        String audience = normalizeText(campaign.getAudienceFocus());

        boolean campaignMatch = interests.stream()
                .anyMatch(term -> (theme != null && theme.contains(term)) || (audience != null && audience.contains(term)));
        if (campaignMatch) {
            return true;
        }

        return safeStream(campaign.getSponsoredPins()).anyMatch(pin -> matchesInterests(pin, interests));
    }

    private boolean matchesInterests(SponsoredPin pin, Set<String> interests) {
        if (interests.isEmpty()) {
            return true;
        }
        Set<String> pinKeywords = safeCopy(pin.getPin() != null ? pin.getPin().getKeywords() : Collections.emptySet());
        Set<String> targetingKeywords = safeCopy(pin.getTargetKeywords());
        String campaignTheme = normalizeText(
                pin.getCampaign() != null ? pin.getCampaign().getCuratedTheme() : null);

        return intersects(pinKeywords, interests)
                || intersects(targetingKeywords, interests)
                || (campaignTheme != null && interests.stream().anyMatch(campaignTheme::contains));
    }

    private boolean intersects(Set<String> source, Set<String> targets) {
        if (CollectionUtils.isEmpty(source) || CollectionUtils.isEmpty(targets)) {
            return false;
        }
        return source.stream().map(this::normalizeToken).anyMatch(targets::contains);
    }

    private Set<String> normalizeInterests(List<String> rawInterests) {
        if (CollectionUtils.isEmpty(rawInterests)) {
          return Collections.emptySet();
        }
        return rawInterests.stream()
                .filter(StringUtils::hasText)
                .flatMap(raw -> Arrays.stream(raw.split(",")))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(this::normalizeToken)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().toLowerCase();
    }

    private String normalizeToken(String token) {
        return token.toLowerCase().replaceAll("\\s+", "-");
    }

    private Set<String> safeCopy(Collection<String> keywords) {
        if (CollectionUtils.isEmpty(keywords)) {
            return Collections.emptySet();
        }
        return keywords.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private <T> Stream<T> safeStream(Collection<T> input) {
        return input == null ? Stream.empty() : input.stream();
    }

    private int resolveLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_SPONSORED_PIN_LIMIT;
        }
        return Math.min(limit, MAX_SPONSORED_PIN_LIMIT);
    }
}

