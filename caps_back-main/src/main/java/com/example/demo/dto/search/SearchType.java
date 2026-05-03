package com.example.demo.dto.search;

public enum SearchType {
    ALL(true, true),
    PINS(true, false),
    BOARDS(false, true);

    private final boolean includesPins;
    private final boolean includesBoards;

    SearchType(boolean includesPins, boolean includesBoards) {
        this.includesPins = includesPins;
        this.includesBoards = includesBoards;
    }

    public boolean includesPins() {
        return includesPins;
    }

    public boolean includesBoards() {
        return includesBoards;
    }

    public static SearchType from(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return ALL;
        }
        try {
            return SearchType.valueOf(rawValue.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ALL;
        }
    }
}

