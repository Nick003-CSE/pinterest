package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

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
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdvertisingServiceTest {

    @Mock
    private SponsoredPinRepository sponsoredPinRepository;

    @Mock
    private AdvertisingCampaignRepository advertisingCampaignRepository;

    @InjectMocks
    private AdvertisingService advertisingService;

    @Test
    void getSponsoredPin_shouldThrow_whenNotFound() {
        when(sponsoredPinRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> advertisingService.getSponsoredPin(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Sponsored pin not found");
    }

    @Test
    void getSponsoredPin_shouldReturnMappedResponse() {
        SponsoredPin sp = buildSponsoredPin(1L, "Running shoes", "running");
        when(sponsoredPinRepository.findById(1L)).thenReturn(Optional.of(sp));

        SponsoredPinResponse response = advertisingService.getSponsoredPin(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getPinId()).isEqualTo(10L);
        assertThat(response.getTitle()).isEqualTo("Running shoes");
    }

    @Test
    void getSponsoredPins_shouldReturnAll_whenNoInterestsProvided() {
        SponsoredPin pin1 = buildSponsoredPin(1L, "Pin 1", "keyword1");
        SponsoredPin pin2 = buildSponsoredPin(2L, "Pin 2", "keyword2");

        when(sponsoredPinRepository.findAllByOrderByPriorityAsc()).thenReturn(Arrays.asList(pin1, pin2));

        List<SponsoredPinResponse> responses = advertisingService.getSponsoredPins(null, null);

        assertThat(responses).hasSize(2);
    }

    @Test
    void getSponsoredPins_shouldRespectLimitAndInterestFiltering() {
        SponsoredPin pin1 = buildSponsoredPin(1L, "Running shoes", "running");
        SponsoredPin pin2 = buildSponsoredPin(2L, "Coffee recipes", "coffee");

        when(sponsoredPinRepository.findAllByOrderByPriorityAsc()).thenReturn(Arrays.asList(pin1, pin2));

        List<SponsoredPinResponse> responses =
                advertisingService.getSponsoredPins(Collections.singletonList("running"), 1);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void getSponsoredPins_shouldUseDefaultLimit_whenLimitIsNull() {
        SponsoredPin pin1 = buildSponsoredPin(1L, "Pin 1", "keyword1");
        when(sponsoredPinRepository.findAllByOrderByPriorityAsc()).thenReturn(Collections.singletonList(pin1));

        List<SponsoredPinResponse> responses = advertisingService.getSponsoredPins(null, null);

        assertThat(responses).hasSize(1);
    }

    @Test
    void getSponsoredPins_shouldCapLimitAtMax() {
        List<SponsoredPin> manyPins = new java.util.ArrayList<>();
        for (long i = 0; i < 60; i++) {
            manyPins.add(buildSponsoredPin(i, "Pin " + i, "keyword"));
        }

        when(sponsoredPinRepository.findAllByOrderByPriorityAsc()).thenReturn(manyPins);

        List<SponsoredPinResponse> responses = advertisingService.getSponsoredPins(null, 100);

        assertThat(responses.size()).isLessThanOrEqualTo(50);
    }

    @Test
    void getSponsoredPins_shouldFilterByInterests() {
        SponsoredPin pin1 = buildSponsoredPin(1L, "Running", "running");
        SponsoredPin pin2 = buildSponsoredPin(2L, "Coffee", "coffee");

        when(sponsoredPinRepository.findAllByOrderByPriorityAsc()).thenReturn(Arrays.asList(pin1, pin2));

        List<SponsoredPinResponse> responses = advertisingService.getSponsoredPins(
                Arrays.asList("running", "fitness"), null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void getSponsoredPinsForCampaign_shouldThrow_whenCampaignNotFound() {
        when(advertisingCampaignRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> advertisingService.getSponsoredPinsForCampaign(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Campaign not found");
    }

    @Test
    void getSponsoredPinsForCampaign_shouldReturnPinsForCampaign() {
        AdvertisingCampaign campaign = buildCampaign(1L, "Test Campaign", "theme");
        when(advertisingCampaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        SponsoredPin pin1 = buildSponsoredPin(10L, "Pin 1", "keyword");
        pin1.setCampaign(campaign);
        SponsoredPin pin2 = buildSponsoredPin(20L, "Pin 2", "keyword");
        pin2.setCampaign(campaign);

        when(sponsoredPinRepository.findByCampaignIdOrderByPriorityAsc(1L))
                .thenReturn(Arrays.asList(pin1, pin2));

        List<SponsoredPinResponse> responses = advertisingService.getSponsoredPinsForCampaign(1L);

        assertThat(responses).hasSize(2);
    }

    @Test
    void getCampaign_shouldThrow_whenNotFound() {
        when(advertisingCampaignRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> advertisingService.getCampaign(42L, false))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Campaign not found");
    }

    @Test
    void getCampaign_shouldReturnResponse_withoutPins_whenIncludePinsFalse() {
        AdvertisingCampaign campaign = buildCampaign(1L, "Test Campaign", "theme");
        when(advertisingCampaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        AdvertisingCampaignResponse response = advertisingService.getCampaign(1L, false);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getFeaturedPins()).isEmpty();
    }

    @Test
    void getCampaign_shouldReturnResponse_withPins_whenIncludePinsTrue() {
        AdvertisingCampaign campaign = buildCampaign(1L, "Test Campaign", "theme");
        SponsoredPin pin1 = buildSponsoredPin(10L, "Pin 1", "keyword");
        pin1.setCampaign(campaign);
        SponsoredPin pin2 = buildSponsoredPin(20L, "Pin 2", "keyword");
        pin2.setCampaign(campaign);
        campaign.setSponsoredPins(Arrays.asList(pin1, pin2));

        when(advertisingCampaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        AdvertisingCampaignResponse response = advertisingService.getCampaign(1L, true);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getFeaturedPins()).hasSize(2);
    }

    @Test
    void getCampaigns_shouldReturnAll_whenNoFilters() {
        AdvertisingCampaign c1 = buildCampaign(1L, "Campaign 1", "theme1");
        AdvertisingCampaign c2 = buildCampaign(2L, "Campaign 2", "theme2");

        when(advertisingCampaignRepository.findAllByOrderByStartDateDesc())
                .thenReturn(Arrays.asList(c1, c2));

        List<AdvertisingCampaignResponse> responses = advertisingService.getCampaigns(null, null, false);

        assertThat(responses).hasSize(2);
    }

    @Test
    void getCampaigns_shouldFilterByStatusAndInterests() {
        AdvertisingCampaign c1 = buildCampaign(1L, "Outdoor Wellness", "running trail");
        c1.setStatus(CampaignStatus.ACTIVE);
        AdvertisingCampaign c2 = buildCampaign(2L, "Coffee Moments", "office coffee");
        c2.setStatus(CampaignStatus.ACTIVE);

        when(advertisingCampaignRepository.findByStatusOrderByStartDateDesc(CampaignStatus.ACTIVE))
                .thenReturn(Arrays.asList(c1, c2));

        List<AdvertisingCampaignResponse> responses = advertisingService.getCampaigns(
                Collections.singletonList("running"), CampaignStatus.ACTIVE, false);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void getCampaigns_shouldFilterByStatusOnly() {
        AdvertisingCampaign c1 = buildCampaign(1L, "Campaign 1", "theme");
        c1.setStatus(CampaignStatus.ACTIVE);
        AdvertisingCampaign c2 = buildCampaign(2L, "Campaign 2", "theme");
        c2.setStatus(CampaignStatus.PAUSED);

        when(advertisingCampaignRepository.findByStatusOrderByStartDateDesc(CampaignStatus.ACTIVE))
                .thenReturn(Collections.singletonList(c1));

        List<AdvertisingCampaignResponse> responses = advertisingService.getCampaigns(
                null, CampaignStatus.ACTIVE, false);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void getCuratedCollections_shouldGroupByTheme() {
        AdvertisingCampaign c1 = buildCampaign(1L, "Outdoor Wellness", "trail");
        c1.setCuratedTheme("Wellness");
        AdvertisingCampaign c2 = buildCampaign(2L, "Indoor Wellness", "yoga");
        c2.setCuratedTheme("Wellness");

        when(advertisingCampaignRepository.findAllByOrderByStartDateDesc())
                .thenReturn(Arrays.asList(c1, c2));

        List<SponsoredCollectionResponse> responses =
                advertisingService.getCuratedCollections(Collections.singletonList("wellness"));

        assertThat(responses).hasSize(1);
        SponsoredCollectionResponse collection = responses.get(0);
        assertThat(collection.getTheme()).isEqualTo("Wellness");
        assertThat(collection.getCampaigns()).hasSize(2);
    }

    @Test
    void getCuratedCollections_shouldFilterByInterests() {
        AdvertisingCampaign c1 = buildCampaign(1L, "Wellness Campaign", "wellness");
        c1.setCuratedTheme("Wellness");
        AdvertisingCampaign c2 = buildCampaign(2L, "Tech Campaign", "technology");
        c2.setCuratedTheme("Technology");

        when(advertisingCampaignRepository.findAllByOrderByStartDateDesc())
                .thenReturn(Arrays.asList(c1, c2));

        List<SponsoredCollectionResponse> responses =
                advertisingService.getCuratedCollections(Collections.singletonList("wellness"));

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getTheme()).isEqualTo("Wellness");
    }

    @Test
    void getCuratedCollections_shouldOnlyIncludeCampaignsWithTheme() {
        AdvertisingCampaign c1 = buildCampaign(1L, "Campaign 1", "theme");
        c1.setCuratedTheme("Wellness");
        AdvertisingCampaign c2 = buildCampaign(2L, "Campaign 2", null);
        c2.setCuratedTheme(null);

        when(advertisingCampaignRepository.findAllByOrderByStartDateDesc())
                .thenReturn(Arrays.asList(c1, c2));

        List<SponsoredCollectionResponse> responses = advertisingService.getCuratedCollections(null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getTheme()).isEqualTo("Wellness");
    }

    @Test
    void getCuratedCollections_shouldSortByTheme() {
        AdvertisingCampaign c1 = buildCampaign(1L, "Campaign 1", "zebra");
        c1.setCuratedTheme("Zebra");
        AdvertisingCampaign c2 = buildCampaign(2L, "Campaign 2", "apple");
        c2.setCuratedTheme("Apple");

        when(advertisingCampaignRepository.findAllByOrderByStartDateDesc())
                .thenReturn(Arrays.asList(c1, c2));

        List<SponsoredCollectionResponse> responses = advertisingService.getCuratedCollections(null);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getTheme()).isEqualTo("Apple");
        assertThat(responses.get(1).getTheme()).isEqualTo("Zebra");
    }

    @Test
    void getCuratedCollections_shouldLimitHighlightedPins() {
        AdvertisingCampaign campaign = buildCampaign(1L, "Campaign", "theme");
        campaign.setCuratedTheme("Theme");
        java.util.List<SponsoredPin> pins = new java.util.ArrayList<>();
        for (long i = 0; i < 10; i++) {
            SponsoredPin pin = buildSponsoredPin(i, "Pin " + i, "keyword");
            pin.setCampaign(campaign);
            pins.add(pin);
        }
        campaign.setSponsoredPins(pins);

        when(advertisingCampaignRepository.findAllByOrderByStartDateDesc())
                .thenReturn(Collections.singletonList(campaign));

        List<SponsoredCollectionResponse> responses = advertisingService.getCuratedCollections(null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getHighlightedPins().size()).isLessThanOrEqualTo(6);
    }

    @Test
    void getCuratedCollections_shouldUseDefaultDescription_whenNoSummary() {
        AdvertisingCampaign campaign = buildCampaign(1L, "Campaign", "theme");
        campaign.setCuratedTheme("Theme");
        campaign.setSummary(null);

        when(advertisingCampaignRepository.findAllByOrderByStartDateDesc())
                .thenReturn(Collections.singletonList(campaign));

        List<SponsoredCollectionResponse> responses = advertisingService.getCuratedCollections(null);

        assertThat(responses.get(0).getDescription())
                .isEqualTo("Curated picks from our advertising partners.");
    }

    private SponsoredPin buildSponsoredPin(Long id, String title, String keyword) {
        SponsoredPin sp = new SponsoredPin();
        sp.setId(id);
        sp.setPriority(1);
        sp.setSponsoredLabel("Sponsored");
        sp.setTargetKeywords(new HashSet<>(Collections.singletonList(keyword)));

        Pin pin = new Pin();
        pin.setId(id * 10);
        pin.setTitle(title);
        pin.setDescription("Desc");
        pin.setMediaUrl("http://example.com/media.jpg");
        pin.setSourceUrl("http://example.com");
        pin.setKeywords(new HashSet<>(Collections.singletonList(keyword)));
        sp.setPin(pin);

        BusinessProfile business = new BusinessProfile();
        business.setId(100L);
        business.setName("Test Brand");
        business.setUsername("testbrand");
        business.setLogoUrl("http://logo.jpg");
        sp.setBusinessProfile(business);

        AdvertisingCampaign campaign = buildCampaign(500L, "Test Campaign", "theme");
        sp.setCampaign(campaign);

        sp.setCreatedAt(Instant.now());
        sp.setUpdatedAt(Instant.now());

        return sp;
    }

    private AdvertisingCampaign buildCampaign(Long id, String headline, String theme) {
        AdvertisingCampaign c = new AdvertisingCampaign();
        c.setId(id);
        c.setName("Campaign " + id);
        c.setHeadline(headline);
        c.setSummary("Summary");
        c.setObjective("Awareness");
        c.setStatus(CampaignStatus.ACTIVE);
        c.setCuratedTheme(theme);
        c.setHeroImageUrl("http://example.com/hero.jpg");
        c.setLandingPageUrl("http://example.com/landing");
        c.setAudienceFocus("Runners and walkers");
        c.setDailyBudget(BigDecimal.TEN);
        c.setPrimaryMetric("CTR");
        c.setStartDate(LocalDate.now().minusDays(10));
        c.setEndDate(LocalDate.now().plusDays(10));
        c.setCreatedAt(Instant.now().minus(Duration.ofDays(1)));
        c.setUpdatedAt(Instant.now());
        c.setSponsoredPins(new java.util.ArrayList<>());
        return c;
    }
}
