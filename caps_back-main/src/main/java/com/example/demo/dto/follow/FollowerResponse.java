package com.example.demo.dto.follow;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FollowerResponse {
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
}

