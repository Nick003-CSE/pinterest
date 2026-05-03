package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GlobalSearchRequest {
    private String keyword;
    private String typeFilter; // "all", "pins", "boards"
    private String sortBy; // "relevance", "recent", "popularity"
    private String sortOrder; // "asc", "desc"
}

