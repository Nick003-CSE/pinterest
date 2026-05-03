package com.example.demo.dto.business;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FollowBusinessRequest {
    @NotNull(message = "Business profile ID is required")
    private Long businessProfileId;
}

