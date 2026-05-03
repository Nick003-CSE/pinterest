package com.example.demo.dto.pin;

import com.example.demo.entity.enums.MediaType;
import com.example.demo.entity.enums.PinStatus;
import com.example.demo.entity.enums.PinVisibility;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PinResponse {
    private Long id;
    private Long ownerId;
    private String ownerUsername;
    private String ownerFullName;
    private Long boardId;
    private String boardName;
    private String title;
    private String description;
    private MediaType mediaType;
    private String mediaUrl;
    private String sourceUrl;
    private String attribution;
    private Set<String> keywords;
    private PinVisibility visibility;
    private PinStatus status;
    private Long saveCount;
    private Long shareCount;
    private Long likeCount;
    private Boolean isLiked;
    private Instant publishedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private List<PinMediaResponse> mediaItems;
}

