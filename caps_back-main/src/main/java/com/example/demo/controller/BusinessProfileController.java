package com.example.demo.controller;

import com.example.demo.dto.business.BusinessProfileResponse;
import com.example.demo.dto.business.FollowBusinessRequest;
import com.example.demo.dto.business.ShowcaseResponse;
import com.example.demo.service.BusinessProfileService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business")
public class BusinessProfileController {

    private final BusinessProfileService businessProfileService;

    @Autowired
    public BusinessProfileController(BusinessProfileService businessProfileService) {
        this.businessProfileService = businessProfileService;
    }

    @GetMapping("/profiles")
    public ResponseEntity<List<BusinessProfileResponse>> getAllBusinessProfiles() {
        return ResponseEntity.ok(businessProfileService.getAllBusinessProfiles());
    }

    @GetMapping("/profiles/{id}")
    public ResponseEntity<BusinessProfileResponse> getBusinessProfile(
            @PathVariable Long id, @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(businessProfileService.getBusinessProfileById(id, userId));
    }

    @GetMapping("/profiles/search")
    public ResponseEntity<List<BusinessProfileResponse>> searchBusinessProfiles(
            @RequestParam String q) {
        return ResponseEntity.ok(businessProfileService.searchBusinessProfiles(q));
    }

    @GetMapping("/profiles/category/{category}")
    public ResponseEntity<List<BusinessProfileResponse>> getBusinessProfilesByCategory(
            @PathVariable String category) {
        return ResponseEntity.ok(businessProfileService.getBusinessProfilesByCategory(category));
    }

    @GetMapping("/profiles/{businessId}/showcases")
    public ResponseEntity<List<ShowcaseResponse>> getShowcasesByBusiness(
            @PathVariable Long businessId) {
        return ResponseEntity.ok(businessProfileService.getShowcasesByBusinessId(businessId));
    }

    @GetMapping("/showcases")
    public ResponseEntity<List<ShowcaseResponse>> getAllShowcases() {
        return ResponseEntity.ok(businessProfileService.getAllShowcases());
    }

    @GetMapping("/showcases/featured")
    public ResponseEntity<List<ShowcaseResponse>> getFeaturedShowcases() {
        return ResponseEntity.ok(businessProfileService.getFeaturedShowcases());
    }

    @GetMapping("/showcases/{id}")
    public ResponseEntity<ShowcaseResponse> getShowcase(@PathVariable Long id) {
        return ResponseEntity.ok(businessProfileService.getShowcaseById(id));
    }

    @PostMapping("/{userId}/follow")
    public ResponseEntity<Void> followBusiness(
            @PathVariable Long userId, @Valid @RequestBody FollowBusinessRequest request) {
        businessProfileService.followBusiness(userId, request.getBusinessProfileId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/unfollow")
    public ResponseEntity<Void> unfollowBusiness(
            @PathVariable Long userId, @Valid @RequestBody FollowBusinessRequest request) {
        businessProfileService.unfollowBusiness(userId, request.getBusinessProfileId());
        return ResponseEntity.ok().build();
    }
}

