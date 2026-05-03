package com.example.demo.dto.board;

import com.example.demo.entity.enums.BoardVisibility;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardResponse {
    private Long id;
    private Long ownerId;
    private String name;
    private String description;
    private BoardVisibility visibility;
    private Long pinCount;
    private Instant createdAt;
    private Instant updatedAt;
    private List<CollaboratorInfo> collaborators;
}

