package com.example.demo.controller;

import com.example.demo.dto.ads.AdvertisingCampaignResponse;
import com.example.demo.dto.ads.SponsoredCollectionResponse;
import com.example.demo.dto.ads.SponsoredPinResponse;
import com.example.demo.entity.enums.CampaignStatus;
import com.example.demo.service.AdvertisingService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ads")
public class AdvertisingController {

    private final AdvertisingService advertisingService;

    public AdvertisingController(AdvertisingService advertisingService) {
        this.advertisingService = advertisingService;
    }

    @GetMapping("/sponsored-pins")
    public ResponseEntity<List<SponsoredPinResponse>> getSponsoredPins(
            @RequestParam(required = false) List<String> interests,
            @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(advertisingService.getSponsoredPins(interests, limit));
    }

    @GetMapping("/sponsored-pins/{id}")
    public ResponseEntity<SponsoredPinResponse> getSponsoredPin(@PathVariable Long id) {
        return ResponseEntity.ok(advertisingService.getSponsoredPin(id));
    }

    @GetMapping("/campaigns")
    public ResponseEntity<List<AdvertisingCampaignResponse>> getCampaigns(
            @RequestParam(required = false, name = "interests") List<String> interests,
            @RequestParam(required = false) CampaignStatus status,
            @RequestParam(defaultValue = "false") boolean includePins) {
        return ResponseEntity.ok(advertisingService.getCampaigns(interests, status, includePins));
    }

    @GetMapping("/campaigns/{id}")
    public ResponseEntity<AdvertisingCampaignResponse> getCampaign(
            @PathVariable Long id, @RequestParam(defaultValue = "false") boolean includePins) {
        return ResponseEntity.ok(advertisingService.getCampaign(id, includePins));
    }

    @GetMapping("/campaigns/{id}/sponsored-pins")
    public ResponseEntity<List<SponsoredPinResponse>> getCampaignPins(@PathVariable Long id) {
        return ResponseEntity.ok(advertisingService.getSponsoredPinsForCampaign(id));
    }

    @GetMapping("/collections")
    public ResponseEntity<List<SponsoredCollectionResponse>> getCollections(
            @RequestParam(required = false, name = "interests") List<String> interests) {
        return ResponseEntity.ok(advertisingService.getCuratedCollections(interests));
    }
}

