package com.example.demo.dto.business;

import lombok.Builder;
import lombok.Getter;
import java.time.Instant;

@Getter
@Builder
public class BusinessProfileResponse {
    private Long id;
    private String name;
    private String username;
    private String description;
    private String logoUrl;
    private String websiteUrl;
    private String category;
    private Boolean verified;
    private Long followerCount;
    private Boolean isFollowing;
    private Instant createdAt;
}

