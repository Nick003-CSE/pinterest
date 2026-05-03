package com.example.demo.dto.board;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchBoardsRequest {
    private Long ownerId;
    private String keyword;
    private String sortBy; // "date", "name"
    private String sortOrder; // "asc", "desc"
}

