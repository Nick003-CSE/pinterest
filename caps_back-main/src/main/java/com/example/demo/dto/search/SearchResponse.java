package com.example.demo.dto.search;

import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchResponse {

    @Builder.Default
    private List<SearchPinResult> pins = Collections.emptyList();

    @Builder.Default
    private List<SearchBoardResult> boards = Collections.emptyList();
}

