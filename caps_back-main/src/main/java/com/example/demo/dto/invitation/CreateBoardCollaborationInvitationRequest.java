package com.example.demo.dto.invitation;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBoardCollaborationInvitationRequest {

    @NotNull(message = "Invitee ID is required")
    private Long inviteeId;

    @NotNull(message = "Board ID is required")
    private Long boardId;

    private String message;
}


