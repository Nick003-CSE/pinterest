package com.example.demo.dto.follow;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FollowRequest {

    @NotNull(message = "Follower id is required")
    private Long followerId;

    @NotNull(message = "Following id is required")
    private Long followingId;
}

