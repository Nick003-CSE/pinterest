package com.example.demo.entity;

import com.example.demo.entity.enums.CampaignStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "advertising_campaigns")
public class AdvertisingCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_profile_id")
    private BusinessProfile businessProfile;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 200)
    private String headline;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(length = 100)
    private String objective;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private CampaignStatus status = CampaignStatus.ACTIVE;

    @Column(name = "curated_theme", length = 120)
    private String curatedTheme;

    @Column(name = "hero_image_url", length = 500)
    private String heroImageUrl;

    @Column(name = "landing_page_url", length = 500)
    private String landingPageUrl;

    @Column(name = "audience_focus", length = 200)
    private String audienceFocus;

    @Column(name = "daily_budget", precision = 12, scale = 2)
    private BigDecimal dailyBudget;

    @Column(name = "primary_metric", length = 120)
    private String primaryMetric;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<SponsoredPin> sponsoredPins = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = Instant.now();
    }
}

