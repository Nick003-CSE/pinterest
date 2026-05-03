package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.dto.business.BusinessProfileResponse;
import com.example.demo.dto.business.ShowcaseResponse;
import com.example.demo.entity.BusinessFollower;
import com.example.demo.entity.BusinessFollowerId;
import com.example.demo.entity.BusinessProfile;
import com.example.demo.entity.Showcase;
import com.example.demo.entity.ShowcasePin;
import com.example.demo.entity.UserAccount;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BusinessFollowerRepository;
import com.example.demo.repository.BusinessProfileRepository;
import com.example.demo.repository.ShowcasePinRepository;
import com.example.demo.repository.ShowcaseRepository;
import com.example.demo.repository.UserAccountRepository;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BusinessProfileServiceTest {

    @Mock
    private BusinessProfileRepository businessProfileRepository;

    @Mock
    private ShowcaseRepository showcaseRepository;

    @Mock
    private BusinessFollowerRepository businessFollowerRepository;

    @Mock
    private ShowcasePinRepository showcasePinRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private BusinessProfileService businessProfileService;

    @Test
    void getBusinessProfileById_shouldThrow_whenNotFound() {
        when(businessProfileRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> businessProfileService.getBusinessProfileById(99L, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Business profile not found");
    }

    @Test
    void getBusinessProfileById_shouldReturnResponse_withFollowerCountAndIsFollowing() {
        BusinessProfile profile = new BusinessProfile();
        profile.setId(1L);
        profile.setName("Nike");
        profile.setUsername("nike");
        profile.setDescription("Just Do It");
        profile.setLogoUrl("http://nike.com/logo.png");
        profile.setWebsiteUrl("http://nike.com");
        profile.setCategory("Fashion");
        profile.setVerified(true);
        profile.setCreatedAt(Instant.now());

        BusinessFollower follower = new BusinessFollower();
        when(businessProfileRepository.findById(1L)).thenReturn(Optional.of(profile));
        when(businessFollowerRepository.findByBusinessProfileId(1L))
                .thenReturn(Collections.singletonList(follower));
        when(businessFollowerRepository.existsById(new BusinessFollowerId(10L, 1L))).thenReturn(true);

        BusinessProfileResponse response = businessProfileService.getBusinessProfileById(1L, 10L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Nike");
        assertThat(response.getUsername()).isEqualTo("nike");
        assertThat(response.getFollowerCount()).isEqualTo(1L);
        assertThat(response.getIsFollowing()).isTrue();
        assertThat(response.getVerified()).isTrue();
    }

    @Test
    void getBusinessProfileById_shouldReturnIsFollowingFalse_whenUserIdIsNull() {
        BusinessProfile profile = new BusinessProfile();
        profile.setId(1L);
        profile.setName("Nike");

        when(businessProfileRepository.findById(1L)).thenReturn(Optional.of(profile));
        when(businessFollowerRepository.findByBusinessProfileId(1L)).thenReturn(Collections.emptyList());

        BusinessProfileResponse response = businessProfileService.getBusinessProfileById(1L, null);

        assertThat(response.getIsFollowing()).isFalse();
        assertThat(response.getFollowerCount()).isEqualTo(0L);
    }

    @Test
    void getBusinessProfileById_shouldReturnIsFollowingFalse_whenNotFollowing() {
        BusinessProfile profile = new BusinessProfile();
        profile.setId(1L);
        profile.setName("Nike");

        when(businessProfileRepository.findById(1L)).thenReturn(Optional.of(profile));
        when(businessFollowerRepository.findByBusinessProfileId(1L)).thenReturn(Collections.emptyList());
        when(businessFollowerRepository.existsById(new BusinessFollowerId(10L, 1L))).thenReturn(false);

        BusinessProfileResponse response = businessProfileService.getBusinessProfileById(1L, 10L);

        assertThat(response.getIsFollowing()).isFalse();
    }

    @Test
    void getAllBusinessProfiles_shouldReturnEmptyList_whenNoProfiles() {
        when(businessProfileRepository.findAll()).thenReturn(Collections.emptyList());

        List<BusinessProfileResponse> result = businessProfileService.getAllBusinessProfiles();

        assertThat(result).isEmpty();
    }

    @Test
    void getAllBusinessProfiles_shouldReturnMappedResponses() {
        BusinessProfile profile1 = new BusinessProfile();
        profile1.setId(1L);
        profile1.setName("Nike");
        profile1.setUsername("nike");

        BusinessProfile profile2 = new BusinessProfile();
        profile2.setId(2L);
        profile2.setName("Adidas");
        profile2.setUsername("adidas");

        when(businessProfileRepository.findAll()).thenReturn(Arrays.asList(profile1, profile2));
        when(businessFollowerRepository.findByBusinessProfileId(1L)).thenReturn(Collections.emptyList());
        when(businessFollowerRepository.findByBusinessProfileId(2L)).thenReturn(Collections.emptyList());

        List<BusinessProfileResponse> result = businessProfileService.getAllBusinessProfiles();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void searchBusinessProfiles_shouldReturnMappedResults() {
        BusinessProfile profile = new BusinessProfile();
        profile.setId(1L);
        profile.setName("Nike Store");

        when(businessProfileRepository.searchBusinessProfiles("nike"))
                .thenReturn(Collections.singletonList(profile));
        when(businessFollowerRepository.findByBusinessProfileId(1L)).thenReturn(Collections.emptyList());

        List<BusinessProfileResponse> result = businessProfileService.searchBusinessProfiles("nike");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Nike Store");
    }

    @Test
    void getBusinessProfilesByCategory_shouldReturnFilteredResults() {
        BusinessProfile profile = new BusinessProfile();
        profile.setId(1L);
        profile.setName("Nike");
        profile.setCategory("Fashion");

        when(businessProfileRepository.findByCategory("Fashion"))
                .thenReturn(Collections.singletonList(profile));
        when(businessFollowerRepository.findByBusinessProfileId(1L)).thenReturn(Collections.emptyList());

        List<BusinessProfileResponse> result = businessProfileService.getBusinessProfilesByCategory("Fashion");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo("Fashion");
    }

    @Test
    void followBusiness_shouldDoNothing_whenAlreadyFollowing() {
        when(businessFollowerRepository.existsById(new BusinessFollowerId(10L, 1L))).thenReturn(true);

        businessProfileService.followBusiness(10L, 1L);

        verify(businessFollowerRepository, never()).save(any(BusinessFollower.class));
        verify(userAccountRepository, never()).findById(any());
        verify(businessProfileRepository, never()).findById(any());
    }

    @Test
    void followBusiness_shouldCreateFollower_whenValid() {
        UserAccount user = new UserAccount();
        user.setId(10L);
        BusinessProfile profile = new BusinessProfile();
        profile.setId(1L);

        when(businessFollowerRepository.existsById(new BusinessFollowerId(10L, 1L))).thenReturn(false);
        when(userAccountRepository.findById(10L)).thenReturn(Optional.of(user));
        when(businessProfileRepository.findById(1L)).thenReturn(Optional.of(profile));

        businessProfileService.followBusiness(10L, 1L);

        ArgumentCaptor<BusinessFollower> captor = ArgumentCaptor.forClass(BusinessFollower.class);
        verify(businessFollowerRepository).save(captor.capture());
        BusinessFollower saved = captor.getValue();
        assertThat(saved.getId().getFollowerId()).isEqualTo(10L);
        assertThat(saved.getId().getBusinessProfileId()).isEqualTo(1L);
    }

    @Test
    void followBusiness_shouldThrow_whenUserNotFound() {
        when(businessFollowerRepository.existsById(new BusinessFollowerId(10L, 1L))).thenReturn(false);
        when(userAccountRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> businessProfileService.followBusiness(10L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void followBusiness_shouldThrow_whenBusinessNotFound() {
        UserAccount user = new UserAccount();
        user.setId(10L);

        when(businessFollowerRepository.existsById(new BusinessFollowerId(10L, 1L))).thenReturn(false);
        when(userAccountRepository.findById(10L)).thenReturn(Optional.of(user));
        when(businessProfileRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> businessProfileService.followBusiness(10L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Business profile not found");
    }

    @Test
    void unfollowBusiness_shouldDeleteFollower() {
        BusinessFollowerId id = new BusinessFollowerId(10L, 1L);
        businessProfileService.unfollowBusiness(10L, 1L);

        verify(businessFollowerRepository).deleteById(id);
    }

    @Test
    void isFollowingBusiness_shouldReturnTrue_whenFollowing() {
        when(businessFollowerRepository.existsById(new BusinessFollowerId(10L, 1L))).thenReturn(true);

        boolean result = businessProfileService.isFollowingBusiness(10L, 1L);

        assertThat(result).isTrue();
    }

    @Test
    void isFollowingBusiness_shouldReturnFalse_whenNotFollowing() {
        when(businessFollowerRepository.existsById(new BusinessFollowerId(10L, 1L))).thenReturn(false);

        boolean result = businessProfileService.isFollowingBusiness(10L, 1L);

        assertThat(result).isFalse();
    }

    @Test
    void getShowcasesByBusinessId_shouldReturnMappedResponses() {
        BusinessProfile profile = new BusinessProfile();
        profile.setId(1L);
        profile.setName("Nike");

        Showcase showcase = new Showcase();
        showcase.setId(100L);
        showcase.setBusinessProfile(profile);
        showcase.setTitle("Spring Collection");
        showcase.setDescription("New arrivals");
        showcase.setTheme("Spring");
        showcase.setCoverImageUrl("http://cover.jpg");
        showcase.setFeatured(true);
        showcase.setCreatedAt(Instant.now());

        ShowcasePin sp = new ShowcasePin();
        sp.setShowcase(showcase);
        sp.setPosition(0);
        com.example.demo.entity.Pin pin = new com.example.demo.entity.Pin();
        pin.setId(200L);
        sp.setPin(pin);

        when(showcaseRepository.findByBusinessProfileIdOrderByFeatured(1L))
                .thenReturn(Collections.singletonList(showcase));
        when(showcasePinRepository.findByShowcaseId(100L))
                .thenReturn(Collections.singletonList(sp));

        List<ShowcaseResponse> responses = businessProfileService.getShowcasesByBusinessId(1L);

        assertThat(responses).hasSize(1);
        ShowcaseResponse response = responses.get(0);
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getBusinessProfileId()).isEqualTo(1L);
        assertThat(response.getBusinessName()).isEqualTo("Nike");
        assertThat(response.getTitle()).isEqualTo("Spring Collection");
        assertThat(response.getPinCount()).isEqualTo(1);
        assertThat(response.getPinIds()).containsExactly(200L);
        assertThat(response.getFeatured()).isTrue();
    }

    @Test
    void getShowcasesByBusinessId_shouldSortPinsByPosition() {
        BusinessProfile profile = new BusinessProfile();
        profile.setId(1L);
        profile.setName("Nike");

        Showcase showcase = new Showcase();
        showcase.setId(100L);
        showcase.setBusinessProfile(profile);

        ShowcasePin sp1 = new ShowcasePin();
        sp1.setPosition(2);
        com.example.demo.entity.Pin pin1 = new com.example.demo.entity.Pin();
        pin1.setId(200L);
        sp1.setPin(pin1);

        ShowcasePin sp2 = new ShowcasePin();
        sp2.setPosition(0);
        com.example.demo.entity.Pin pin2 = new com.example.demo.entity.Pin();
        pin2.setId(201L);
        sp2.setPin(pin2);

        ShowcasePin sp3 = new ShowcasePin();
        sp3.setPosition(1);
        com.example.demo.entity.Pin pin3 = new com.example.demo.entity.Pin();
        pin3.setId(202L);
        sp3.setPin(pin3);

        when(showcaseRepository.findByBusinessProfileIdOrderByFeatured(1L))
                .thenReturn(Collections.singletonList(showcase));
        when(showcasePinRepository.findByShowcaseId(100L))
                .thenReturn(Arrays.asList(sp1, sp2, sp3));

        List<ShowcaseResponse> responses = businessProfileService.getShowcasesByBusinessId(1L);

        assertThat(responses.get(0).getPinIds()).containsExactly(201L, 202L, 200L);
    }

    @Test
    void getAllShowcases_shouldReturnAllShowcases() {
        BusinessProfile profile1 = new BusinessProfile();
        profile1.setId(10L);
        profile1.setName("Business 1");

        BusinessProfile profile2 = new BusinessProfile();
        profile2.setId(20L);
        profile2.setName("Business 2");

        Showcase showcase1 = new Showcase();
        showcase1.setId(1L);
        showcase1.setBusinessProfile(profile1);
        Showcase showcase2 = new Showcase();
        showcase2.setId(2L);
        showcase2.setBusinessProfile(profile2);

        when(showcaseRepository.findAll()).thenReturn(Arrays.asList(showcase1, showcase2));
        when(showcasePinRepository.findByShowcaseId(1L)).thenReturn(Collections.emptyList());
        when(showcasePinRepository.findByShowcaseId(2L)).thenReturn(Collections.emptyList());

        List<ShowcaseResponse> result = businessProfileService.getAllShowcases();

        assertThat(result).hasSize(2);
    }

    @Test
    void getFeaturedShowcases_shouldReturnOnlyFeatured() {
        BusinessProfile profile = new BusinessProfile();
        profile.setId(10L);
        profile.setName("Business");

        Showcase featured = new Showcase();
        featured.setId(1L);
        featured.setFeatured(true);
        featured.setBusinessProfile(profile);

        when(showcaseRepository.findByFeaturedTrue()).thenReturn(Collections.singletonList(featured));
        when(showcasePinRepository.findByShowcaseId(1L)).thenReturn(Collections.emptyList());

        List<ShowcaseResponse> result = businessProfileService.getFeaturedShowcases();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFeatured()).isTrue();
    }

    @Test
    void getShowcaseById_shouldThrow_whenNotFound() {
        when(showcaseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> businessProfileService.getShowcaseById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Showcase not found");
    }

    @Test
    void getShowcaseById_shouldReturnMappedResponse() {
        BusinessProfile profile = new BusinessProfile();
        profile.setId(1L);
        profile.setName("Nike");

        Showcase showcase = new Showcase();
        showcase.setId(100L);
        showcase.setBusinessProfile(profile);
        showcase.setTitle("Summer Collection");

        when(showcaseRepository.findById(100L)).thenReturn(Optional.of(showcase));
        when(showcasePinRepository.findByShowcaseId(100L)).thenReturn(Collections.emptyList());

        ShowcaseResponse response = businessProfileService.getShowcaseById(100L);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getTitle()).isEqualTo("Summer Collection");
    }
}
