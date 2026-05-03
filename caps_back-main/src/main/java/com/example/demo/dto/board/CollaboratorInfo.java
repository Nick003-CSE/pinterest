package com.example.demo.dto.board;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CollaboratorInfo {
    private Long id;
    private String username;
    private String fullName;
    private String avatarUrl;
}

