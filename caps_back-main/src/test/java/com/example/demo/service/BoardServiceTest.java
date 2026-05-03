package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.dto.board.BoardResponse;
import com.example.demo.dto.board.CreateBoardRequest;
import com.example.demo.dto.board.ReorderBoardsRequest;
import com.example.demo.dto.board.SearchBoardsRequest;
import com.example.demo.dto.board.UpdateBoardRequest;
import com.example.demo.entity.Board;
import com.example.demo.entity.BoardCollaboration;
import com.example.demo.entity.UserAccount;
import com.example.demo.entity.enums.BoardVisibility;
import com.example.demo.exception.BusinessValidationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BoardCollaborationRepository;
import com.example.demo.repository.BoardRepository;
import com.example.demo.repository.PinRepository;
import com.example.demo.repository.UserAccountRepository;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PinRepository pinRepository;

    @Mock
    private BoardCollaborationRepository boardCollaborationRepository;

    @InjectMocks
    private BoardService boardService;

    @Test
    void createBoardEntity_shouldThrow_whenOwnerNotFound() {
        CreateBoardRequest request = new CreateBoardRequest();
        request.setOwnerId(1L);
        request.setName("My Board");

        when(userAccountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.createBoardEntity(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Owner not found");
    }

    @Test
    void createBoardEntity_shouldThrow_whenDuplicateNameForOwner() {
        CreateBoardRequest request = new CreateBoardRequest();
        request.setOwnerId(1L);
        request.setName("My Board");

        UserAccount owner = new UserAccount();
        owner.setId(1L);

        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(boardRepository.existsByOwnerIdAndNameIgnoreCase(1L, "My Board")).thenReturn(true);

        assertThatThrownBy(() -> boardService.createBoardEntity(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createBoardEntity_shouldSaveBoard_whenValid() {
        CreateBoardRequest request = new CreateBoardRequest();
        request.setOwnerId(1L);
        request.setName("My Board");
        request.setDescription("Desc");

        UserAccount owner = new UserAccount();
        owner.setId(1L);

        Board saved = new Board();
        saved.setId(10L);
        saved.setOwner(owner);
        saved.setName("My Board");

        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(boardRepository.existsByOwnerIdAndNameIgnoreCase(1L, "My Board")).thenReturn(false);
        when(boardRepository.save(any(Board.class))).thenReturn(saved);

        Board result = boardService.createBoardEntity(request);

        assertThat(result.getId()).isEqualTo(10L);
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    void getBoardEntity_shouldThrow_whenNotFound() {
        when(boardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.getBoardEntity(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Board not found");
    }

    @Test
    void updateBoard_shouldUpdateFields_whenPresentInRequest() {
        Board board = new Board();
        board.setId(10L);
        board.setName("Old");
        board.setDescription("Old desc");
        board.setCreatedAt(Instant.now());
        board.setUpdatedAt(Instant.now());
        UserAccount owner = new UserAccount();
        owner.setId(1L);
        board.setOwner(owner);

        UpdateBoardRequest request = new UpdateBoardRequest();
        request.setName("New Name");
        request.setDescription("New Desc");

        when(boardRepository.findById(10L)).thenReturn(Optional.of(board));
        when(boardRepository.save(any(Board.class))).thenReturn(board);
        when(boardCollaborationRepository.findByBoardId(10L)).thenReturn(Collections.emptyList());

        boardService.updateBoard(10L, request);

        assertThat(board.getName()).isEqualTo("New Name");
        assertThat(board.getDescription()).isEqualTo("New Desc");
        verify(boardRepository).save(board);
    }

    @Test
    void updateBoard_shouldLeaveFieldsUnchanged_whenNullInRequest() {
        Board board = new Board();
        board.setId(10L);
        board.setName("Old");
        board.setDescription("Old desc");
        board.setVisibility(BoardVisibility.PUBLIC);
        UserAccount owner = new UserAccount();
        owner.setId(1L);
        board.setOwner(owner);

        UpdateBoardRequest request = new UpdateBoardRequest();
        // all fields null

        when(boardRepository.findById(10L)).thenReturn(Optional.of(board));
        when(boardRepository.save(any(Board.class))).thenReturn(board);

        BoardResponse response = boardService.updateBoard(10L, request);

        assertThat(board.getName()).isEqualTo("Old");
        assertThat(board.getDescription()).isEqualTo("Old desc");
        assertThat(board.getVisibility()).isEqualTo(BoardVisibility.PUBLIC);
        assertThat(response.getName()).isEqualTo("Old");
    }

    @Test
    void reorderBoards_shouldThrow_whenBoardDoesNotBelongToOwner() {
        Board board1 = new Board();
        UserAccount owner1 = new UserAccount();
        owner1.setId(1L);
        board1.setId(1L);
        board1.setOwner(owner1);

        Board board2 = new Board();
        UserAccount owner2 = new UserAccount();
        owner2.setId(2L);
        board2.setId(2L);
        board2.setOwner(owner2);

        ReorderBoardsRequest request = new ReorderBoardsRequest();
        request.setBoardIds(Arrays.asList(1L, 2L));

        when(boardRepository.findAllById(Arrays.asList(1L, 2L))).thenReturn(Arrays.asList(board1, board2));

        assertThatThrownBy(() -> boardService.reorderBoards(1L, request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("does not belong to owner");
    }

    @Test
    void reorderBoards_shouldUpdatePositions_whenValid() {
        UserAccount owner = new UserAccount();
        owner.setId(1L);

        Board board1 = new Board();
        board1.setId(1L);
        board1.setOwner(owner);
        board1.setPosition(0);

        Board board2 = new Board();
        board2.setId(2L);
        board2.setOwner(owner);
        board2.setPosition(1);

        ReorderBoardsRequest request = new ReorderBoardsRequest();
        request.setBoardIds(Arrays.asList(2L, 1L));

        when(boardRepository.findAllById(Arrays.asList(2L, 1L))).thenReturn(Arrays.asList(board1, board2));
        when(boardRepository.findByOwnerId(1L)).thenReturn(Arrays.asList(board1, board2));
        when(boardCollaborationRepository.findByCollaboratorId(1L)).thenReturn(Collections.emptyList());

        boardService.reorderBoards(1L, request);

        ArgumentCaptor<List<Board>> captor = ArgumentCaptor.forClass((Class) List.class);
        verify(boardRepository).saveAll(captor.capture());
        List<Board> saved = captor.getValue();

        // Board with id 2 should now have position 0, id 1 position 1
        Board savedBoard2 = saved.stream().filter(b -> b.getId().equals(2L)).findFirst().orElseThrow();
        Board savedBoard1 = saved.stream().filter(b -> b.getId().equals(1L)).findFirst().orElseThrow();
        assertThat(savedBoard2.getPosition()).isEqualTo(0);
        assertThat(savedBoard1.getPosition()).isEqualTo(1);
    }

    @Test
    void deleteBoard_shouldDeletePinsThenBoard() {
        Board board = new Board();
        board.setId(5L);
        UserAccount owner = new UserAccount();
        owner.setId(1L);
        board.setOwner(owner);

        when(boardRepository.findById(5L)).thenReturn(Optional.of(board));

        boardService.deleteBoard(5L);

        verify(pinRepository).findByBoardId(5L);
        verify(boardRepository).delete(board);
    }

    @Test
    void getBoardById_shouldReturnMappedResponse() {
        Board board = new Board();
        board.setId(1L);
        UserAccount owner = new UserAccount();
        owner.setId(10L);
        board.setOwner(owner);
        board.setName("My Board");
        board.setDescription("Desc");
        board.setVisibility(BoardVisibility.PUBLIC);
        board.setCreatedAt(Instant.now());
        board.setUpdatedAt(Instant.now());

        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));
        when(pinRepository.countByBoardId(1L)).thenReturn(3L);
        when(boardCollaborationRepository.findByBoardId(1L)).thenReturn(Collections.emptyList());

        BoardResponse response = boardService.getBoardById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getOwnerId()).isEqualTo(10L);
        assertThat(response.getPinCount()).isEqualTo(3L);
    }

    @Test
    void getBoardsForOwner_shouldIncludeOwnedAndCollaboratedBoards() {
        Long ownerId = 1L;

        UserAccount owner = new UserAccount();
        owner.setId(ownerId);

        Board ownedBoard = new Board();
        ownedBoard.setId(10L);
        ownedBoard.setOwner(owner);
        ownedBoard.setName("Owned");
        ownedBoard.setPosition(0);
        ownedBoard.setCreatedAt(Instant.now());
        ownedBoard.setUpdatedAt(Instant.now());
        ownedBoard.setVisibility(BoardVisibility.PUBLIC);

        Board collabBoard = new Board();
        collabBoard.setId(20L);
        collabBoard.setOwner(owner);
        collabBoard.setName("Collab");
        collabBoard.setPosition(1);
        collabBoard.setCreatedAt(Instant.now());
        collabBoard.setUpdatedAt(Instant.now());
        collabBoard.setVisibility(BoardVisibility.PUBLIC);

        UserAccount collaboratorUser = new UserAccount();
        collaboratorUser.setId(ownerId);

        BoardCollaboration collaboration = new BoardCollaboration();
        collaboration.setBoard(collabBoard);
        collaboration.setCollaborator(collaboratorUser);

        when(boardRepository.findByOwnerId(ownerId)).thenReturn(Collections.singletonList(ownedBoard));
        when(boardCollaborationRepository.findByCollaboratorId(ownerId))
                .thenReturn(Collections.singletonList(collaboration));
        when(pinRepository.countByBoardId(any(Long.class))).thenReturn(0L);
        when(boardCollaborationRepository.findByBoardId(any(Long.class))).thenReturn(Collections.emptyList());

        List<BoardResponse> responses = boardService.getBoardsForOwner(ownerId);

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(BoardResponse::getName).containsExactly("Owned", "Collab");
    }

    @Test
    void searchBoards_shouldApplyExplicitSorting_whenSortParamsProvided() {
        SearchBoardsRequest request = new SearchBoardsRequest();
        request.setOwnerId(1L);
        request.setKeyword("test");
        request.setSortBy("name");
        request.setSortOrder("asc");

        Board boardA = new Board();
        boardA.setId(1L);
        boardA.setName("Alpha");
        boardA.setOwner(new UserAccount());
        boardA.setCreatedAt(Instant.now());
        boardA.setUpdatedAt(Instant.now());
        boardA.setVisibility(BoardVisibility.PUBLIC);

        Board boardB = new Board();
        boardB.setId(2L);
        boardB.setName("Beta");
        boardB.setOwner(new UserAccount());
        boardB.setCreatedAt(Instant.now());
        boardB.setUpdatedAt(Instant.now());
        boardB.setVisibility(BoardVisibility.PUBLIC);

        when(boardRepository.searchBoards(1L, "test")).thenReturn(Arrays.asList(boardB, boardA));
        when(pinRepository.countByBoardId(any(Long.class))).thenReturn(0L);
        when(boardCollaborationRepository.findByBoardId(any(Long.class))).thenReturn(Collections.emptyList());

        List<BoardResponse> responses = boardService.searchBoards(request);

        assertThat(responses).extracting(BoardResponse::getName).containsExactly("Alpha", "Beta");
    }

    @Test
    void searchBoards_shouldDefaultSortByPosition_whenNoSortParams() {
        SearchBoardsRequest request = new SearchBoardsRequest();
        request.setOwnerId(1L);
        request.setKeyword(null);

        Board board1 = new Board();
        board1.setId(1L);
        board1.setName("First");
        board1.setPosition(1);
        board1.setOwner(new UserAccount());
        board1.setCreatedAt(Instant.now());
        board1.setUpdatedAt(Instant.now());
        board1.setVisibility(BoardVisibility.PUBLIC);

        Board board2 = new Board();
        board2.setId(2L);
        board2.setName("Zero");
        board2.setPosition(0);
        board2.setOwner(new UserAccount());
        board2.setCreatedAt(Instant.now());
        board2.setUpdatedAt(Instant.now());
        board2.setVisibility(BoardVisibility.PUBLIC);

        when(boardRepository.searchBoards(1L, null)).thenReturn(Arrays.asList(board1, board2));
        when(pinRepository.countByBoardId(any(Long.class))).thenReturn(0L);
        when(boardCollaborationRepository.findByBoardId(any(Long.class))).thenReturn(Collections.emptyList());

        List<BoardResponse> responses = boardService.searchBoards(request);

        assertThat(responses).extracting(BoardResponse::getName).containsExactly("Zero", "First");
    }

    @Test
    void sortBoards_shouldSortByDateDescending() {
        Instant now = Instant.now();
        BoardResponse older = BoardResponse.builder()
                .id(1L)
                .name("A")
                .createdAt(now.minusSeconds(60))
                .build();

        BoardResponse newer = BoardResponse.builder()
                .id(2L)
                .name("B")
                .createdAt(now)
                .build();

        List<BoardResponse> sorted = boardService.sortBoards(Arrays.asList(older, newer), "date", "desc");

        assertThat(sorted).extracting(BoardResponse::getId).containsExactly(2L, 1L);
    }

    @Test
    void sortBoards_shouldReturnOriginal_whenUnknownSortBy() {
        BoardResponse b1 = BoardResponse.builder().id(1L).name("A").build();
        BoardResponse b2 = BoardResponse.builder().id(2L).name("B").build();

        List<BoardResponse> sorted = boardService.sortBoards(Arrays.asList(b1, b2), "unknown", "asc");

        assertThat(sorted).containsExactly(b1, b2);
    }

    @Test
    void searchPublicBoards_shouldDelegateToRepository() {
        when(boardRepository.searchPublicBoards("kw")).thenReturn(Collections.emptyList());

        List<Board> result = boardService.searchPublicBoards("kw");

        assertThat(result).isEmpty();
        verify(boardRepository).searchPublicBoards("kw");
    }

    @Test
    void searchPublicBoardsWithSuggestions_shouldDelegateToRepository() {
        when(boardRepository.searchPublicBoardsWithSuggestions("kw")).thenReturn(Collections.emptyList());

        List<Board> result = boardService.searchPublicBoardsWithSuggestions("kw");

        assertThat(result).isEmpty();
        verify(boardRepository).searchPublicBoardsWithSuggestions("kw");
    }

    @Test
    void reorderBoards_shouldThrow_whenBoardIdMissingInLoadedBoards() {
        UserAccount owner = new UserAccount();
        owner.setId(1L);

        Board board1 = new Board();
        board1.setId(1L);
        board1.setOwner(owner);

        ReorderBoardsRequest request = new ReorderBoardsRequest();
        request.setBoardIds(Arrays.asList(1L, 2L));

        // Repository returns only board1; boardId 2L is missing
        when(boardRepository.findAllById(Arrays.asList(1L, 2L))).thenReturn(Collections.singletonList(board1));

        assertThatThrownBy(() -> boardService.reorderBoards(1L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Board not found: 2");
    }
}


