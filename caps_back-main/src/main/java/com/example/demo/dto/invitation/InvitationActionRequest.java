package com.example.demo.dto.invitation;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvitationActionRequest {
    @NotNull(message = "Invitation ID is required")
    private Long invitationId;
}

