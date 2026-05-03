package com.example.demo.service;

import com.example.demo.dto.business.BusinessProfileResponse;
import com.example.demo.dto.business.ShowcaseResponse;
import com.example.demo.entity.BusinessFollower;
import com.example.demo.entity.BusinessFollowerId;
import com.example.demo.entity.BusinessProfile;
import com.example.demo.entity.Showcase;
import com.example.demo.entity.ShowcasePin;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.entity.UserAccount;
import com.example.demo.repository.BusinessFollowerRepository;
import com.example.demo.repository.BusinessProfileRepository;
import com.example.demo.repository.ShowcasePinRepository;
import com.example.demo.repository.ShowcaseRepository;
import com.example.demo.repository.UserAccountRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BusinessProfileService {

    private final BusinessProfileRepository businessProfileRepository;
    private final ShowcaseRepository showcaseRepository;
    private final BusinessFollowerRepository businessFollowerRepository;
    private final ShowcasePinRepository showcasePinRepository;
    private final UserAccountRepository userAccountRepository;

    @Autowired
    public BusinessProfileService(
            BusinessProfileRepository businessProfileRepository,
            ShowcaseRepository showcaseRepository,
            BusinessFollowerRepository businessFollowerRepository,
            ShowcasePinRepository showcasePinRepository,
            UserAccountRepository userAccountRepository) {
        this.businessProfileRepository = businessProfileRepository;
        this.showcaseRepository = showcaseRepository;
        this.businessFollowerRepository = businessFollowerRepository;
        this.showcasePinRepository = showcasePinRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional(readOnly = true)
    public List<BusinessProfileResponse> getAllBusinessProfiles() {
        return businessProfileRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BusinessProfileResponse getBusinessProfileById(Long id, Long userId) {
        BusinessProfile business = businessProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Business profile not found: " + id));
        return mapToResponse(business, userId);
    }

    @Transactional(readOnly = true)
    public List<BusinessProfileResponse> searchBusinessProfiles(String searchTerm) {
        return businessProfileRepository.searchBusinessProfiles(searchTerm).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BusinessProfileResponse> getBusinessProfilesByCategory(String category) {
        return businessProfileRepository.findByCategory(category).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ShowcaseResponse> getShowcasesByBusinessId(Long businessId) {
        List<Showcase> showcases = showcaseRepository.findByBusinessProfileIdOrderByFeatured(businessId);
        return showcases.stream()
                .map(this::mapShowcaseToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ShowcaseResponse> getAllShowcases() {
        return showcaseRepository.findAll().stream()
                .map(this::mapShowcaseToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ShowcaseResponse> getFeaturedShowcases() {
        return showcaseRepository.findByFeaturedTrue().stream()
                .map(this::mapShowcaseToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ShowcaseResponse getShowcaseById(Long showcaseId) {
        Showcase showcase = showcaseRepository.findById(showcaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Showcase not found: " + showcaseId));
        return mapShowcaseToResponse(showcase);
    }

    @Transactional
    public void followBusiness(Long userId, Long businessProfileId) {
        BusinessFollowerId id = new BusinessFollowerId(userId, businessProfileId);
        if (businessFollowerRepository.existsById(id)) {
            return; // Already following
        }

        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        BusinessProfile business = businessProfileRepository.findById(businessProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Business profile not found: " + businessProfileId));

        BusinessFollower follower = new BusinessFollower();
        follower.setId(id);
        follower.setFollower(user);
        follower.setBusinessProfile(business);
        businessFollowerRepository.save(follower);
    }

    @Transactional
    public void unfollowBusiness(Long userId, Long businessProfileId) {
        BusinessFollowerId id = new BusinessFollowerId(userId, businessProfileId);
        businessFollowerRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean isFollowingBusiness(Long userId, Long businessProfileId) {
        BusinessFollowerId id = new BusinessFollowerId(userId, businessProfileId);
        return businessFollowerRepository.existsById(id);
    }

    private BusinessProfileResponse mapToResponse(BusinessProfile business) {
        return mapToResponse(business, null);
    }

    private BusinessProfileResponse mapToResponse(BusinessProfile business, Long userId) {
        Long followerCount = (long) businessFollowerRepository.findByBusinessProfileId(business.getId()).size();
        Boolean isFollowing = userId != null && isFollowingBusiness(userId, business.getId());

        return BusinessProfileResponse.builder()
                .id(business.getId())
                .name(business.getName())
                .username(business.getUsername())
                .description(business.getDescription())
                .logoUrl(business.getLogoUrl())
                .websiteUrl(business.getWebsiteUrl())
                .category(business.getCategory())
                .verified(business.getVerified())
                .followerCount(followerCount)
                .isFollowing(isFollowing)
                .createdAt(business.getCreatedAt())
                .build();
    }

    private ShowcaseResponse mapShowcaseToResponse(Showcase showcase) {
        List<ShowcasePin> showcasePins = showcasePinRepository.findByShowcaseId(showcase.getId());
        List<Long> pinIds = showcasePins.stream()
                .sorted((a, b) -> Integer.compare(a.getPosition(), b.getPosition()))
                .map(sp -> sp.getPin().getId())
                .collect(Collectors.toList());

        return ShowcaseResponse.builder()
                .id(showcase.getId())
                .businessProfileId(showcase.getBusinessProfile().getId())
                .businessName(showcase.getBusinessProfile().getName())
                .title(showcase.getTitle())
                .description(showcase.getDescription())
                .theme(showcase.getTheme())
                .coverImageUrl(showcase.getCoverImageUrl())
                .featured(showcase.getFeatured())
                .pinCount(pinIds.size())
                .pinIds(pinIds)
                .createdAt(showcase.getCreatedAt())
                .build();
    }
}

