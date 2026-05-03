package com.example.demo.repository;

import com.example.demo.entity.AdvertisingCampaign;
import com.example.demo.entity.enums.CampaignStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdvertisingCampaignRepository extends JpaRepository<AdvertisingCampaign, Long> {
    List<AdvertisingCampaign> findAllByOrderByStartDateDesc();

    List<AdvertisingCampaign> findByStatusOrderByStartDateDesc(CampaignStatus status);
}

