package com.example.demo.entity;

import com.example.demo.entity.enums.MediaType;
import com.example.demo.entity.enums.PinStatus;
import com.example.demo.entity.enums.PinVisibility;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pins")
public class Pin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private UserAccount owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", length = 10)
    private MediaType mediaType = MediaType.IMAGE;

    @Column(name = "media_url", columnDefinition = "LONGTEXT")
    private String mediaUrl;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(length = 255)
    private String attribution;

    @ElementCollection
    @CollectionTable(name = "pin_keywords", joinColumns = @JoinColumn(name = "pin_id"))
    @Column(name = "keyword", length = 50)
    private Set<String> keywords = new LinkedHashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", length = 10)
    private PinVisibility visibility = PinVisibility.PUBLIC;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10)
    private PinStatus status = PinStatus.DRAFT;

    @Column(name = "save_count", nullable = false)
    private Long saveCount = 0L;

    @Column(name = "share_count", nullable = false)
    private Long shareCount = 0L;

    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "pin", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PinMedia> mediaItems = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == PinStatus.PUBLISHED && publishedAt == null) {
            publishedAt = now;
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = Instant.now();
        if (status == PinStatus.PUBLISHED && publishedAt == null) {
            publishedAt = Instant.now();
        }
    }

    public void addMediaItem(PinMedia media) {
        mediaItems.add(media);
        media.setPin(this);
    }

    public void clearMediaItems() {
        mediaItems.forEach(m -> m.setPin(null));
        mediaItems.clear();
    }
}

