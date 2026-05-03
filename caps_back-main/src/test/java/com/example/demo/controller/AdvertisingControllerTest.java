package com.example.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.dto.ads.AdvertisingCampaignResponse;
import com.example.demo.dto.ads.SponsoredCollectionResponse;
import com.example.demo.dto.ads.SponsoredPinResponse;
import com.example.demo.entity.enums.CampaignStatus;
import com.example.demo.service.AdvertisingService;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AdvertisingControllerTest {

    @Mock
    private AdvertisingService advertisingService;

    @InjectMocks
    private AdvertisingController advertisingController;

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(advertisingController).build();
    }

    @Test
    void getSponsoredPins_shouldReturnListOfSponsoredPins() throws Exception {
        SponsoredPinResponse pin1 = SponsoredPinResponse.builder()
                .id(1L)
                .title("Sponsored Pin 1")
                .build();
        SponsoredPinResponse pin2 = SponsoredPinResponse.builder()
                .id(2L)
                .title("Sponsored Pin 2")
                .build();
        List<SponsoredPinResponse> pins = Arrays.asList(pin1, pin2);

        when(advertisingService.getSponsoredPins(isNull(), isNull())).thenReturn(pins);

        mockMvc()
                .perform(get("/ads/sponsored-pins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Sponsored Pin 1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].title").value("Sponsored Pin 2"));

        verify(advertisingService).getSponsoredPins(null, null);
    }

    @Test
    void getSponsoredPins_withInterests_shouldFilterByInterests() throws Exception {
        List<String> interests = Arrays.asList("fitness", "wellness");
        when(advertisingService.getSponsoredPins(eq(interests), any())).thenReturn(Collections.emptyList());

        mockMvc()
                .perform(get("/ads/sponsored-pins")
                        .param("interests", "fitness", "wellness"))
                .andExpect(status().isOk());

        verify(advertisingService).getSponsoredPins(interests, null);
    }

    @Test
    void getSponsoredPin_shouldReturnSingleSponsoredPin() throws Exception {
        SponsoredPinResponse pin = SponsoredPinResponse.builder()
                .id(1L)
                .title("Sponsored Pin")
                .build();

        when(advertisingService.getSponsoredPin(1L)).thenReturn(pin);

        mockMvc()
                .perform(get("/ads/sponsored-pins/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Sponsored Pin"));

        verify(advertisingService).getSponsoredPin(1L);
    }

    @Test
    void getCampaigns_shouldReturnListOfCampaigns() throws Exception {
        AdvertisingCampaignResponse campaign = AdvertisingCampaignResponse.builder()
                .id(1L)
                .headline("Campaign 1")
                .status(CampaignStatus.ACTIVE)
                .build();
        List<AdvertisingCampaignResponse> campaigns = Collections.singletonList(campaign);

        when(advertisingService.getCampaigns(isNull(), isNull(), eq(false))).thenReturn(campaigns);

        mockMvc()
                .perform(get("/ads/campaigns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].headline").value("Campaign 1"));

        verify(advertisingService).getCampaigns(null, null, false);
    }

    @Test
    void getCampaign_shouldReturnSingleCampaign() throws Exception {
        AdvertisingCampaignResponse campaign = AdvertisingCampaignResponse.builder()
                .id(1L)
                .headline("Campaign")
                .build();

        when(advertisingService.getCampaign(1L, false)).thenReturn(campaign);

        mockMvc()
                .perform(get("/ads/campaigns/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.headline").value("Campaign"));

        verify(advertisingService).getCampaign(1L, false);
    }

    @Test
    void getCampaignPins_shouldReturnListOfSponsoredPins() throws Exception {
        SponsoredPinResponse pin = SponsoredPinResponse.builder()
                .id(1L)
                .title("Pin")
                .build();
        List<SponsoredPinResponse> pins = Collections.singletonList(pin);

        when(advertisingService.getSponsoredPinsForCampaign(1L)).thenReturn(pins);

        mockMvc()
                .perform(get("/ads/campaigns/1/sponsored-pins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(advertisingService).getSponsoredPinsForCampaign(1L);
    }

    @Test
    void getCollections_shouldReturnListOfCollections() throws Exception {
        SponsoredCollectionResponse collection = SponsoredCollectionResponse.builder()
                .theme("Collection Theme")
                .description("Collection Description")
                .build();
        List<SponsoredCollectionResponse> collections = Collections.singletonList(collection);

        when(advertisingService.getCuratedCollections(isNull())).thenReturn(collections);

        mockMvc()
                .perform(get("/ads/collections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].theme").value("Collection Theme"));

        verify(advertisingService).getCuratedCollections(null);
    }
}

