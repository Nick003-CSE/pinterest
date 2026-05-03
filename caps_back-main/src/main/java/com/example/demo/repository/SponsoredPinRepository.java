package com.example.demo.repository;

import com.example.demo.entity.SponsoredPin;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SponsoredPinRepository extends JpaRepository<SponsoredPin, Long> {

    List<SponsoredPin> findAllByOrderByPriorityAsc();

    List<SponsoredPin> findByFeaturedTrueOrderByPriorityAsc();

    List<SponsoredPin> findByCampaignIdOrderByPriorityAsc(Long campaignId);
}

