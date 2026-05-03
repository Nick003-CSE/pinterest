package com.example.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.dto.business.BusinessProfileResponse;
import com.example.demo.dto.business.FollowBusinessRequest;
import com.example.demo.dto.business.ShowcaseResponse;
import com.example.demo.service.BusinessProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class BusinessProfileControllerTest {

    @Mock
    private BusinessProfileService businessProfileService;

    @InjectMocks
    private BusinessProfileController businessProfileController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(businessProfileController).build();
    }

    @Test
    void getAllBusinessProfiles_shouldReturnListOfProfiles() throws Exception {
        BusinessProfileResponse profile = BusinessProfileResponse.builder()
                .id(1L)
                .username("business1")
                .name("Business 1")
                .build();
        List<BusinessProfileResponse> profiles = Collections.singletonList(profile);

        when(businessProfileService.getAllBusinessProfiles()).thenReturn(profiles);

        mockMvc()
                .perform(get("/business/profiles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].username").value("business1"));

        verify(businessProfileService).getAllBusinessProfiles();
    }

    @Test
    void getBusinessProfile_shouldReturnProfile() throws Exception {
        BusinessProfileResponse profile = BusinessProfileResponse.builder()
                .id(1L)
                .username("business1")
                .build();

        when(businessProfileService.getBusinessProfileById(1L, null)).thenReturn(profile);

        mockMvc()
                .perform(get("/business/profiles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("business1"));

        verify(businessProfileService).getBusinessProfileById(1L, null);
    }

    @Test
    void getBusinessProfile_withUserId_shouldReturnProfile() throws Exception {
        BusinessProfileResponse profile = BusinessProfileResponse.builder()
                .id(1L)
                .username("business1")
                .build();

        when(businessProfileService.getBusinessProfileById(1L, 2L)).thenReturn(profile);

        mockMvc()
                .perform(get("/business/profiles/1").param("userId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(businessProfileService).getBusinessProfileById(1L, 2L);
    }

    @Test
    void searchBusinessProfiles_shouldReturnFilteredProfiles() throws Exception {
        BusinessProfileResponse profile = BusinessProfileResponse.builder()
                .id(1L)
                .name("Test Business")
                .build();
        List<BusinessProfileResponse> profiles = Collections.singletonList(profile);

        when(businessProfileService.searchBusinessProfiles("test")).thenReturn(profiles);

        mockMvc()
                .perform(get("/business/profiles/search").param("q", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(businessProfileService).searchBusinessProfiles("test");
    }

    @Test
    void getBusinessProfilesByCategory_shouldReturnFilteredProfiles() throws Exception {
        BusinessProfileResponse profile = BusinessProfileResponse.builder()
                .id(1L)
                .category("Technology")
                .build();
        List<BusinessProfileResponse> profiles = Collections.singletonList(profile);

        when(businessProfileService.getBusinessProfilesByCategory("Technology")).thenReturn(profiles);

        mockMvc()
                .perform(get("/business/profiles/category/Technology"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(businessProfileService).getBusinessProfilesByCategory("Technology");
    }

    @Test
    void getShowcasesByBusiness_shouldReturnListOfShowcases() throws Exception {
        ShowcaseResponse showcase = ShowcaseResponse.builder()
                .id(1L)
                .title("Showcase 1")
                .build();
        List<ShowcaseResponse> showcases = Collections.singletonList(showcase);

        when(businessProfileService.getShowcasesByBusinessId(1L)).thenReturn(showcases);

        mockMvc()
                .perform(get("/business/profiles/1/showcases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(businessProfileService).getShowcasesByBusinessId(1L);
    }

    @Test
    void getAllShowcases_shouldReturnListOfShowcases() throws Exception {
        ShowcaseResponse showcase = ShowcaseResponse.builder()
                .id(1L)
                .title("Showcase")
                .build();
        List<ShowcaseResponse> showcases = Collections.singletonList(showcase);

        when(businessProfileService.getAllShowcases()).thenReturn(showcases);

        mockMvc()
                .perform(get("/business/showcases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(businessProfileService).getAllShowcases();
    }

    @Test
    void getFeaturedShowcases_shouldReturnFeaturedShowcases() throws Exception {
        ShowcaseResponse showcase = ShowcaseResponse.builder()
                .id(1L)
                .title("Featured Showcase")
                .featured(true)
                .build();
        List<ShowcaseResponse> showcases = Collections.singletonList(showcase);

        when(businessProfileService.getFeaturedShowcases()).thenReturn(showcases);

        mockMvc()
                .perform(get("/business/showcases/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].featured").value(true));

        verify(businessProfileService).getFeaturedShowcases();
    }

    @Test
    void getShowcase_shouldReturnSingleShowcase() throws Exception {
        ShowcaseResponse showcase = ShowcaseResponse.builder()
                .id(1L)
                .title("Showcase")
                .build();

        when(businessProfileService.getShowcaseById(1L)).thenReturn(showcase);

        mockMvc()
                .perform(get("/business/showcases/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Showcase"));

        verify(businessProfileService).getShowcaseById(1L);
    }

    @Test
    void followBusiness_shouldReturnOk() throws Exception {
        FollowBusinessRequest request = new FollowBusinessRequest();
        request.setBusinessProfileId(1L);

        mockMvc()
                .perform(post("/business/2/follow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(businessProfileService).followBusiness(2L, 1L);
    }

    @Test
    void unfollowBusiness_shouldReturnOk() throws Exception {
        FollowBusinessRequest request = new FollowBusinessRequest();
        request.setBusinessProfileId(1L);

        mockMvc()
                .perform(post("/business/2/unfollow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(businessProfileService).unfollowBusiness(2L, 1L);
    }
}

