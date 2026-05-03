package com.example.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.dto.invitation.CreateBoardCollaborationInvitationRequest;
import com.example.demo.dto.invitation.InvitationActionRequest;
import com.example.demo.dto.invitation.InvitationResponse;
import com.example.demo.service.InvitationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class InvitationControllerTest {

    @Mock
    private InvitationService invitationService;

    @InjectMocks
    private InvitationController invitationController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(invitationController).build();
    }

    @Test
    void getPendingInvitations_shouldReturnListOfPendingInvitations() throws Exception {
        InvitationResponse invitation = InvitationResponse.builder()
                .id(1L)
                .inviterId(2L)
                .inviterName("User Two")
                .inviterUsername("user2")
                .inviterAvatar("http://avatar.com/user2.jpg")
                .type("Board Collaboration")
                .status("pending")
                .message("Please collaborate")
                .boardId(10L)
                .boardTitle("My Board")
                .sentAt(Instant.now())
                .build();
        List<InvitationResponse> invitations = Collections.singletonList(invitation);

        when(invitationService.getPendingInvitations(1L)).thenReturn(invitations);

        mockMvc()
                .perform(get("/invitations/1/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("pending"))
                .andExpect(jsonPath("$[0].inviterId").value(2L))
                .andExpect(jsonPath("$[0].boardId").value(10L));

        verify(invitationService).getPendingInvitations(1L);
    }

    @Test
    void getPendingInvitations_shouldReturnEmptyList_whenNoPendingInvitations() throws Exception {
        when(invitationService.getPendingInvitations(1L)).thenReturn(Collections.emptyList());

        mockMvc()
                .perform(get("/invitations/1/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(invitationService).getPendingInvitations(1L);
    }

    @Test
    void getPendingInvitations_shouldReturnMultipleInvitations() throws Exception {
        InvitationResponse inv1 = InvitationResponse.builder()
                .id(1L)
                .status("pending")
                .build();
        InvitationResponse inv2 = InvitationResponse.builder()
                .id(2L)
                .status("pending")
                .build();

        when(invitationService.getPendingInvitations(1L)).thenReturn(Arrays.asList(inv1, inv2));

        mockMvc()
                .perform(get("/invitations/1/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(invitationService).getPendingInvitations(1L);
    }

    @Test
    void getAllInvitations_shouldReturnAllInvitations() throws Exception {
        InvitationResponse invitation1 = InvitationResponse.builder()
                .id(1L)
                .status("pending")
                .build();
        InvitationResponse invitation2 = InvitationResponse.builder()
                .id(2L)
                .status("accepted")
                .build();
        InvitationResponse invitation3 = InvitationResponse.builder()
                .id(3L)
                .status("declined")
                .build();
        List<InvitationResponse> invitations = Arrays.asList(invitation1, invitation2, invitation3);

        when(invitationService.getAllInvitations(1L)).thenReturn(invitations);

        mockMvc()
                .perform(get("/invitations/1/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[2].id").value(3L))
                .andExpect(jsonPath("$[0].status").value("pending"))
                .andExpect(jsonPath("$[1].status").value("accepted"))
                .andExpect(jsonPath("$[2].status").value("declined"));

        verify(invitationService).getAllInvitations(1L);
    }

    @Test
    void getAllInvitations_shouldReturnEmptyList_whenNoInvitations() throws Exception {
        when(invitationService.getAllInvitations(1L)).thenReturn(Collections.emptyList());

        mockMvc()
                .perform(get("/invitations/1/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(invitationService).getAllInvitations(1L);
    }

    @Test
    void acceptInvitation_shouldReturnAcceptedInvitation() throws Exception {
        InvitationActionRequest request = new InvitationActionRequest();
        request.setInvitationId(1L);

        InvitationResponse response = InvitationResponse.builder()
                .id(1L)
                .status("accepted")
                .inviterId(2L)
                .boardId(10L)
                .build();

        when(invitationService.acceptInvitation(1L, 2L)).thenReturn(response);

        mockMvc()
                .perform(post("/invitations/2/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("accepted"))
                .andExpect(jsonPath("$.inviterId").value(2L))
                .andExpect(jsonPath("$.boardId").value(10L));

        verify(invitationService).acceptInvitation(1L, 2L);
    }

    @Test
    void acceptInvitation_shouldHandleInvalidJson() throws Exception {
        String invalidBody = "{ invalid json }";

        mockMvc()
                .perform(post("/invitations/2/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void acceptInvitation_shouldHandleMissingInvitationId() throws Exception {
        String body = "{}";

        mockMvc()
                .perform(post("/invitations/2/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void declineInvitation_shouldReturnDeclinedInvitation() throws Exception {
        InvitationActionRequest request = new InvitationActionRequest();
        request.setInvitationId(1L);

        InvitationResponse response = InvitationResponse.builder()
                .id(1L)
                .status("declined")
                .build();

        when(invitationService.declineInvitation(1L, 2L)).thenReturn(response);

        mockMvc()
                .perform(post("/invitations/2/decline")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("declined"));

        verify(invitationService).declineInvitation(1L, 2L);
    }

    @Test
    void declineInvitation_shouldHandleInvalidJson() throws Exception {
        String invalidBody = "{ invalid }";

        mockMvc()
                .perform(post("/invitations/2/decline")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBoardCollaborationInvitation_shouldReturnCreatedInvitation() throws Exception {
        CreateBoardCollaborationInvitationRequest request = new CreateBoardCollaborationInvitationRequest();
        request.setInviteeId(3L);
        request.setBoardId(1L);
        request.setMessage("Please collaborate on my board");

        InvitationResponse response = InvitationResponse.builder()
                .id(1L)
                .inviterId(2L)
                .inviterName("Inviter Name")
                .inviterUsername("inviter")
                .boardId(1L)
                .boardTitle("My Board")
                .type("Board Collaboration")
                .status("pending")
                .message("Please collaborate on my board")
                .sentAt(Instant.now())
                .build();

        when(invitationService.createBoardCollaborationInvitation(eq(2L), eq(3L), eq(1L), any(String.class)))
                .thenReturn(response);

        mockMvc()
                .perform(post("/invitations/2/board-collaboration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.type").value("Board Collaboration"))
                .andExpect(jsonPath("$.status").value("pending"))
                .andExpect(jsonPath("$.boardId").value(1L))
                .andExpect(jsonPath("$.message").value("Please collaborate on my board"));

        verify(invitationService).createBoardCollaborationInvitation(eq(2L), eq(3L), eq(1L), any(String.class));
    }

    @Test
    void createBoardCollaborationInvitation_shouldHandleMissingFields() throws Exception {
        CreateBoardCollaborationInvitationRequest request = new CreateBoardCollaborationInvitationRequest();
        request.setInviteeId(3L);
        // Missing boardId

        mockMvc()
                .perform(post("/invitations/2/board-collaboration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBoardCollaborationInvitation_shouldHandleNullMessage() throws Exception {
        CreateBoardCollaborationInvitationRequest request = new CreateBoardCollaborationInvitationRequest();
        request.setInviteeId(3L);
        request.setBoardId(1L);
        request.setMessage(null);

        InvitationResponse response = InvitationResponse.builder()
                .id(1L)
                .status("pending")
                .build();

        when(invitationService.createBoardCollaborationInvitation(eq(2L), eq(3L), eq(1L), any()))
                .thenReturn(response);

        mockMvc()
                .perform(post("/invitations/2/board-collaboration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(invitationService).createBoardCollaborationInvitation(eq(2L), eq(3L), eq(1L), any());
    }

    @Test
    void createBoardCollaborationInvitation_shouldHandleEmptyMessage() throws Exception {
        CreateBoardCollaborationInvitationRequest request = new CreateBoardCollaborationInvitationRequest();
        request.setInviteeId(3L);
        request.setBoardId(1L);
        request.setMessage("");

        InvitationResponse response = InvitationResponse.builder()
                .id(1L)
                .status("pending")
                .build();

        when(invitationService.createBoardCollaborationInvitation(eq(2L), eq(3L), eq(1L), any()))
                .thenReturn(response);

        mockMvc()
                .perform(post("/invitations/2/board-collaboration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(invitationService).createBoardCollaborationInvitation(eq(2L), eq(3L), eq(1L), any());
    }
}
