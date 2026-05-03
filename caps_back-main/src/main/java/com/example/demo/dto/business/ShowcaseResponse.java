package com.example.demo.dto.business;

import lombok.Builder;
import lombok.Getter;
import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class ShowcaseResponse {
    private Long id;
    private Long businessProfileId;
    private String businessName;
    private String title;
    private String description;
    private String theme;
    private String coverImageUrl;
    private Boolean featured;
    private Integer pinCount;
    private List<Long> pinIds;
    private Instant createdAt;
}

