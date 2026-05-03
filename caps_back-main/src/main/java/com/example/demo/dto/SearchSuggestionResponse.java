package com.example.demo.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchSuggestionResponse {
    private String id;
    private String label;
    private String type; // "Pin", "Board", "Keyword"
    private String thumbnail;
    private String subtitle;
    private String pinId;
    private String boardId;
}

