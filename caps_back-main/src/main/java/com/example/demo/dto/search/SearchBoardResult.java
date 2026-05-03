package com.example.demo.dto.search;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchBoardResult {
    private Long id;
    private String name;
    private String description;
    private String ownerName;
    private String ownerUsername;
    private String coverUrl;
    private Instant createdAt;
    private Instant updatedAt;
}

