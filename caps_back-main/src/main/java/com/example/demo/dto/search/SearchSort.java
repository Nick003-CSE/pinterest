package com.example.demo.dto.search;

public enum SearchSort {
    RELEVANCE,
    RECENT;

    public static SearchSort from(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return RELEVANCE;
        }
        try {
            return SearchSort.valueOf(rawValue.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return RELEVANCE;
        }
    }
}

