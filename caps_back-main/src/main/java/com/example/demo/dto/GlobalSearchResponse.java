package com.example.demo.dto;

import com.example.demo.dto.board.BoardResponse;
import com.example.demo.dto.pin.PinResponse;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GlobalSearchResponse {
    private List<PinResponse> pins;
    private List<BoardResponse> boards;
    private Long totalResults;
    private String query;
}

