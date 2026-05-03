package com.example.demo.controller;

import com.example.demo.dto.invitation.CreateBoardCollaborationInvitationRequest;
import com.example.demo.dto.invitation.InvitationActionRequest;
import com.example.demo.dto.invitation.InvitationResponse;
import com.example.demo.service.InvitationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/invitations")
public class InvitationController {

    private final InvitationService invitationService;

    @Autowired
    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @GetMapping("/{userId}/pending")
    public ResponseEntity<List<InvitationResponse>> getPendingInvitations(@PathVariable Long userId) {
        return ResponseEntity.ok(invitationService.getPendingInvitations(userId));
    }

    @GetMapping("/{userId}/all")
    public ResponseEntity<List<InvitationResponse>> getAllInvitations(@PathVariable Long userId) {
        return ResponseEntity.ok(invitationService.getAllInvitations(userId));
    }

    @PostMapping("/{userId}/accept")
    public ResponseEntity<InvitationResponse> acceptInvitation(
            @PathVariable Long userId, @Valid @RequestBody InvitationActionRequest request) {
        InvitationResponse response = invitationService.acceptInvitation(request.getInvitationId(), userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/decline")
    public ResponseEntity<InvitationResponse> declineInvitation(
            @PathVariable Long userId, @Valid @RequestBody InvitationActionRequest request) {
        InvitationResponse response = invitationService.declineInvitation(request.getInvitationId(), userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/board-collaboration")
    public ResponseEntity<InvitationResponse> createBoardCollaborationInvitation(
            @PathVariable Long userId,
            @Valid @RequestBody CreateBoardCollaborationInvitationRequest request) {
        InvitationResponse response =
                invitationService.createBoardCollaborationInvitation(
                        userId, request.getInviteeId(), request.getBoardId(), request.getMessage());
        return ResponseEntity.ok(response);
    }
}

