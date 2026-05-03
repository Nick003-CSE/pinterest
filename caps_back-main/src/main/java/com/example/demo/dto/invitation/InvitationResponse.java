package com.example.demo.dto.invitation;

import lombok.Builder;
import lombok.Getter;
import java.time.Instant;

@Getter
@Builder
public class InvitationResponse {
    private Long id;
    private Long inviterId;
    private String inviterName;
    private String inviterUsername;
    private String inviterAvatar;
    private Long boardId;
    private String boardTitle;
    private String type;
    private String message;
    private String status;
    private Instant sentAt;
}

