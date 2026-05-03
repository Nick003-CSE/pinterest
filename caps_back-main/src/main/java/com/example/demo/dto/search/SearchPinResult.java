package com.example.demo.dto.search;

import java.time.Instant;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchPinResult {
    private Long id;
    private Long boardId;
    private String boardName;
    private String title;
    private String description;
    private String thumbnailUrl;
    private String ownerName;
    private String ownerUsername;
    private Set<String> keywords;
    private Instant createdAt;
}

