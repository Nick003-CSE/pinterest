package com.example.demo.dto.pin;

import com.example.demo.entity.enums.PinStatus;
import com.example.demo.entity.enums.PinVisibility;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchPinsRequest {
    private Long ownerId;
    private String keyword;
    private PinStatus status;
    private PinVisibility visibility;
    private String sortBy; // "date", "title"
    private String sortOrder; // "asc", "desc"
}

