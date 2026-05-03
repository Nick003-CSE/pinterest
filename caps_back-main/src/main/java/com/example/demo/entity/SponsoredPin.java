package com.example.demo.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sponsored_pins")
public class SponsoredPin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pin_id", unique = true)
    private Pin pin;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_profile_id")
    private BusinessProfile businessProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id")
    private AdvertisingCampaign campaign;

    @Column(name = "sponsored_label", length = 80)
    private String sponsoredLabel = "Sponsored";

    @Column(name = "cta_text", length = 120)
    private String ctaText;

    @Column(name = "cta_url", length = 500)
    private String ctaUrl;

    @Column(name = "priority")
    private Integer priority = 0;

    @Column(name = "featured")
    private Boolean featured = Boolean.FALSE;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "sponsored_pin_keywords", joinColumns = @JoinColumn(name = "sponsored_pin_id"))
    @Column(name = "keyword", length = 50)
    private Set<String> targetKeywords = new LinkedHashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

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

