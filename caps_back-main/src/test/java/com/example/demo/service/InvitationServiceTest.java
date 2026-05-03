package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.dto.invitation.InvitationResponse;
import com.example.demo.entity.Board;
import com.example.demo.entity.Invitation;
import com.example.demo.entity.Invitation.InvitationStatus;
import com.example.demo.entity.Invitation.InvitationType;
import com.example.demo.entity.UserAccount;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BoardCollaborationRepository;
import com.example.demo.repository.BoardRepository;
import com.example.demo.repository.InvitationRepository;
import com.example.demo.repository.UserAccountRepository;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private BoardCollaborationRepository boardCollaborationRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private InvitationService invitationService;

    @Test
    void getPendingInvitations_shouldReturnResponses() {
        Invitation invitation = buildInvitation(1L, 10L, 20L, InvitationStatus.PENDING);
        when(invitationRepository.findByInviteeIdAndStatus(20L, InvitationStatus.PENDING))
                .thenReturn(Collections.singletonList(invitation));

        assertThat(invitationService.getPendingInvitations(20L)).hasSize(1);
    }

    @Test
    void acceptInvitation_shouldThrow_whenNotFound() {
        when(invitationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invitationService.acceptInvitation(99L, 20L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Invitation not found");
    }

    @Test
    void acceptInvitation_shouldThrow_whenDifferentInvitee() {
        Invitation invitation = buildInvitation(1L, 10L, 20L, InvitationStatus.PENDING);
        when(invitationRepository.findById(1L)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.acceptInvitation(1L, 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("only accept invitations sent to you");
    }

    @Test
    void acceptInvitation_shouldUpdateStatus_toAccepted() {
        Invitation invitation = buildInvitation(1L, 10L, 20L, InvitationStatus.PENDING);
        when(invitationRepository.findById(1L)).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(invitation)).thenReturn(invitation);
        when(boardCollaborationRepository.existsById(any())).thenReturn(false);

        InvitationResponse response = invitationService.acceptInvitation(1L, 20L);

        assertThat(response.getStatus()).isEqualTo(InvitationStatus.ACCEPTED.name());
    }

    @Test
    void declineInvitation_shouldUpdateStatus_toDeclined() {
        Invitation invitation = buildInvitation(1L, 10L, 20L, InvitationStatus.PENDING);
        when(invitationRepository.findById(1L)).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(invitation)).thenReturn(invitation);

        InvitationResponse response = invitationService.declineInvitation(1L, 20L);

        assertThat(response.getStatus()).isEqualTo(InvitationStatus.DECLINED.name());
    }

    @Test
    void getAllInvitations_shouldReturnResponses() {
        Invitation invitation = buildInvitation(1L, 10L, 20L, InvitationStatus.PENDING);
        when(invitationRepository.findByInviteeId(20L))
                .thenReturn(Collections.singletonList(invitation));

        assertThat(invitationService.getAllInvitations(20L))
                .hasSize(1)
                .first()
                .extracting(InvitationResponse::getId)
                .isEqualTo(1L);
    }

    @Test
    void createBoardCollaborationInvitation_shouldThrow_whenInvitingSelf() {
        assertThatThrownBy(
                        () -> invitationService.createBoardCollaborationInvitation(10L, 10L, 100L, "msg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot invite yourself");
    }

    @Test
    void createBoardCollaborationInvitation_shouldThrow_whenBoardNotFound() {
        when(boardRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(
                        () -> invitationService.createBoardCollaborationInvitation(10L, 20L, 100L, "msg"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Board not found");
    }

    @Test
    void createBoardCollaborationInvitation_shouldThrow_whenInviterIsNotOwner() {
        UserAccount owner = new UserAccount();
        owner.setId(99L);

        Board board = new Board();
        board.setId(100L);
        board.setOwner(owner);

        when(boardRepository.findById(100L)).thenReturn(Optional.of(board));

        assertThatThrownBy(
                        () -> invitationService.createBoardCollaborationInvitation(10L, 20L, 100L, "msg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only the board owner can invite collaborators");
    }

    @Test
    void createBoardCollaborationInvitation_shouldCreateInvitation_whenValid() {
        Long inviterId = 10L;
        Long inviteeId = 20L;
        Long boardId = 100L;

        UserAccount owner = new UserAccount();
        owner.setId(inviterId);

        Board board = new Board();
        board.setId(boardId);
        board.setOwner(owner);

        UserAccount inviter = new UserAccount();
        inviter.setId(inviterId);
        inviter.setFullName("Inviter");
        inviter.setUsername("inviter");

        UserAccount invitee = new UserAccount();
        invitee.setId(inviteeId);
        invitee.setFullName("Invitee");
        invitee.setUsername("invitee");

        Invitation saved = new Invitation();
        saved.setId(1L);
        saved.setInviter(inviter);
        saved.setInvitee(invitee);
        saved.setBoard(board);
        saved.setType(InvitationType.BOARD_COLLABORATION);
        saved.setMessage("Join my board");
        saved.setStatus(InvitationStatus.PENDING);
        saved.setCreatedAt(Instant.now());

        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(userAccountRepository.findById(inviterId)).thenReturn(Optional.of(inviter));
        when(userAccountRepository.findById(inviteeId)).thenReturn(Optional.of(invitee));
        when(invitationRepository.save(any(Invitation.class))).thenReturn(saved);

        InvitationResponse response =
                invitationService.createBoardCollaborationInvitation(inviterId, inviteeId, boardId, "Join my board");

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getInviterId()).isEqualTo(inviterId);
        assertThat(response.getBoardId()).isEqualTo(boardId);
        assertThat(response.getStatus()).isEqualTo(InvitationStatus.PENDING.name());

        verify(invitationRepository).save(any(Invitation.class));
    }

    private Invitation buildInvitation(Long id, Long inviterId, Long inviteeId, InvitationStatus status) {
        UserAccount inviter = new UserAccount();
        inviter.setId(inviterId);
        inviter.setFullName("Inviter");
        inviter.setUsername("inviter");

        UserAccount invitee = new UserAccount();
        invitee.setId(inviteeId);

        Board board = new Board();
        board.setId(100L);
        board.setName("Board");

        Invitation inv = new Invitation();
        inv.setId(id);
        inv.setInviter(inviter);
        inv.setInvitee(invitee);
        inv.setBoard(board);
        inv.setType(InvitationType.BOARD_COLLABORATION);
        inv.setMessage("Join my board");
        inv.setStatus(status);
        inv.setCreatedAt(Instant.now());
        return inv;
    }
}


