package com.example.demo.service;

import com.example.demo.dto.invitation.InvitationResponse;
import com.example.demo.entity.Board;
import com.example.demo.entity.BoardCollaboration;
import com.example.demo.entity.BoardCollaborationId;
import com.example.demo.entity.Invitation;
import com.example.demo.entity.Invitation.InvitationStatus;
import com.example.demo.entity.Invitation.InvitationType;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BoardCollaborationRepository;
import com.example.demo.repository.InvitationRepository;
import com.example.demo.repository.BoardRepository;
import com.example.demo.repository.UserAccountRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final BoardCollaborationRepository boardCollaborationRepository;
    private final BoardRepository boardRepository;
    private final UserAccountRepository userAccountRepository;

    @Autowired
    public InvitationService(
            InvitationRepository invitationRepository,
            BoardCollaborationRepository boardCollaborationRepository,
            BoardRepository boardRepository,
            UserAccountRepository userAccountRepository) {
        this.invitationRepository = invitationRepository;
        this.boardCollaborationRepository = boardCollaborationRepository;
        this.boardRepository = boardRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional(readOnly = true)
    public List<InvitationResponse> getPendingInvitations(Long userId) {
        List<Invitation> invitations =
                invitationRepository.findByInviteeIdAndStatus(userId, InvitationStatus.PENDING);
        return invitations.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InvitationResponse> getAllInvitations(Long userId) {
        List<Invitation> invitations = invitationRepository.findByInviteeId(userId);
        return invitations.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public InvitationResponse acceptInvitation(Long invitationId, Long userId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found: " + invitationId));

        if (!invitation.getInvitee().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only accept invitations sent to you");
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalArgumentException("Invitation has already been responded to");
        }

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation = invitationRepository.save(invitation);

        // If this is a board collaboration, register the invitee as a collaborator on the board
        if (invitation.getType() == InvitationType.BOARD_COLLABORATION
                && invitation.getBoard() != null) {
            Board board = invitation.getBoard();
            Long collaboratorId = invitation.getInvitee().getId();
            BoardCollaborationId id = new BoardCollaborationId(board.getId(), collaboratorId);

            if (!boardCollaborationRepository.existsById(id)) {
                BoardCollaboration collaboration = new BoardCollaboration();
                collaboration.setId(id);
                collaboration.setBoard(board);
                collaboration.setCollaborator(invitation.getInvitee());
                boardCollaborationRepository.save(collaboration);
            }
        }

        return mapToResponse(invitation);
    }

    @Transactional
    public InvitationResponse createBoardCollaborationInvitation(
            Long inviterId, Long inviteeId, Long boardId, String message) {
        if (inviterId.equals(inviteeId)) {
            throw new IllegalArgumentException("You cannot invite yourself to collaborate on a board");
        }

        Board board = boardRepository
                .findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found: " + boardId));

        if (!board.getOwner().getId().equals(inviterId)) {
            throw new IllegalArgumentException("Only the board owner can invite collaborators");
        }

        Invitation invitation = new Invitation();
        invitation.setInviter(userAccountRepository
                .findById(inviterId)
                .orElseThrow(() -> new ResourceNotFoundException("Inviter not found: " + inviterId)));
        invitation.setInvitee(userAccountRepository
                .findById(inviteeId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitee not found: " + inviteeId)));
        invitation.setBoard(board);
        invitation.setType(InvitationType.BOARD_COLLABORATION);
        invitation.setMessage(message);
        invitation.setStatus(InvitationStatus.PENDING);

        Invitation saved = invitationRepository.save(invitation);
        return mapToResponse(saved);
    }

    @Transactional
    public InvitationResponse declineInvitation(Long invitationId, Long userId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found: " + invitationId));

        if (!invitation.getInvitee().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only decline invitations sent to you");
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalArgumentException("Invitation has already been responded to");
        }

        invitation.setStatus(InvitationStatus.DECLINED);
        invitation = invitationRepository.save(invitation);

        return mapToResponse(invitation);
    }

    private InvitationResponse mapToResponse(Invitation invitation) {
        String type = invitation.getType() == InvitationType.BOARD_COLLABORATION
                ? "Board Collaboration"
                : "Connection";

        String inviterAvatar = "https://ui-avatars.com/api/?background=0f172a&color=fff&name="
                + java.net.URLEncoder.encode(invitation.getInviter().getFullName(), java.nio.charset.StandardCharsets.UTF_8);

        return InvitationResponse.builder()
                .id(invitation.getId())
                .inviterId(invitation.getInviter().getId())
                .inviterName(invitation.getInviter().getFullName())
                .inviterUsername(invitation.getInviter().getUsername())
                .inviterAvatar(inviterAvatar)
                .boardId(invitation.getBoard() != null ? invitation.getBoard().getId() : null)
                .boardTitle(invitation.getBoard() != null ? invitation.getBoard().getName() : null)
                .type(type)
                .message(invitation.getMessage())
                .status(invitation.getStatus().name())
                .sentAt(invitation.getCreatedAt())
                .build();
    }
}

