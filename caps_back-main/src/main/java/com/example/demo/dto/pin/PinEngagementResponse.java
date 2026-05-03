package com.example.demo.dto.pin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PinEngagementResponse {
    private Long pinId;
    private Long saveCount;
    private Long shareCount;
    private Long likeCount;
    private Boolean isLiked;
}


