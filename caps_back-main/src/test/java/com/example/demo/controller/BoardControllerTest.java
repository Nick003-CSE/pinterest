package com.example.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.dto.board.BoardResponse;
import com.example.demo.dto.board.CreateBoardRequest;
import com.example.demo.dto.board.ReorderBoardsRequest;
import com.example.demo.dto.board.SearchBoardsRequest;
import com.example.demo.dto.board.UpdateBoardRequest;
import com.example.demo.entity.enums.BoardVisibility;
import com.example.demo.service.BoardService;
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
class BoardControllerTest {

    @Mock
    private BoardService boardService;

    @InjectMocks
    private BoardController boardController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(boardController).build();
    }

    @Test
    void createBoard_shouldReturnCreatedBoard() throws Exception {
        CreateBoardRequest request = new CreateBoardRequest();
        request.setOwnerId(1L);
        request.setName("Test Board");
        request.setDescription("Description");
        request.setVisibility(BoardVisibility.PUBLIC);

        BoardResponse response = BoardResponse.builder()
                .id(1L)
                .ownerId(1L)
                .name("Test Board")
                .description("Description")
                .visibility(BoardVisibility.PUBLIC)
                .pinCount(0L)
                .createdAt(Instant.now())
                .build();

        when(boardService.createBoard(any(CreateBoardRequest.class))).thenReturn(response);

        mockMvc()
                .perform(post("/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Board"))
                .andExpect(jsonPath("$.visibility").value("PUBLIC"));

        verify(boardService).createBoard(any(CreateBoardRequest.class));
    }

    @Test
    void createBoard_shouldHandleInvalidJson() throws Exception {
        String invalidBody = "{ invalid json }";

        mockMvc()
                .perform(post("/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBoard_shouldHandleMissingRequiredFields() throws Exception {
        CreateBoardRequest request = new CreateBoardRequest();
        request.setOwnerId(1L);
        // Missing name

        mockMvc()
                .perform(post("/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBoardById_shouldReturnBoard() throws Exception {
        BoardResponse response = BoardResponse.builder()
                .id(1L)
                .name("Test Board")
                .ownerId(10L)
                .pinCount(5L)
                .visibility(BoardVisibility.PUBLIC)
                .build();

        when(boardService.getBoardById(1L)).thenReturn(response);

        mockMvc()
                .perform(get("/boards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Board"))
                .andExpect(jsonPath("$.ownerId").value(10L))
                .andExpect(jsonPath("$.pinCount").value(5L));

        verify(boardService).getBoardById(1L);
    }

    @Test
    void getBoardsForOwner_shouldReturnListOfBoards() throws Exception {
        BoardResponse board1 = BoardResponse.builder().id(1L).name("Board 1").build();
        BoardResponse board2 = BoardResponse.builder().id(2L).name("Board 2").build();
        List<BoardResponse> boards = Arrays.asList(board1, board2);

        when(boardService.getBoardsForOwner(1L)).thenReturn(boards);

        mockMvc()
                .perform(get("/boards/owner/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(boardService).getBoardsForOwner(1L);
    }

    @Test
    void getBoardsForOwner_shouldReturnEmptyList_whenNoBoards() throws Exception {
        when(boardService.getBoardsForOwner(1L)).thenReturn(Collections.emptyList());

        mockMvc()
                .perform(get("/boards/owner/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(boardService).getBoardsForOwner(1L);
    }

    @Test
    void searchBoards_shouldReturnFilteredBoards() throws Exception {
        SearchBoardsRequest request = new SearchBoardsRequest();
        request.setOwnerId(1L);
        request.setKeyword("test");
        request.setSortBy("name");
        request.setSortOrder("asc");

        BoardResponse board = BoardResponse.builder().id(1L).name("Test Board").build();
        List<BoardResponse> boards = Collections.singletonList(board);

        when(boardService.searchBoards(any(SearchBoardsRequest.class))).thenReturn(boards);

        mockMvc()
                .perform(post("/boards/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(boardService).searchBoards(any(SearchBoardsRequest.class));
    }

    @Test
    void searchBoards_shouldHandleInvalidJson() throws Exception {
        String invalidBody = "{ invalid }";

        mockMvc()
                .perform(post("/boards/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateBoard_shouldReturnUpdatedBoard() throws Exception {
        UpdateBoardRequest request = new UpdateBoardRequest();
        request.setName("Updated Board");
        request.setDescription("Updated Description");
        request.setVisibility(BoardVisibility.PRIVATE);

        BoardResponse response = BoardResponse.builder()
                .id(1L)
                .name("Updated Board")
                .description("Updated Description")
                .visibility(BoardVisibility.PRIVATE)
                .build();

        when(boardService.updateBoard(eq(1L), any(UpdateBoardRequest.class))).thenReturn(response);

        mockMvc()
                .perform(put("/boards/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Board"))
                .andExpect(jsonPath("$.visibility").value("PRIVATE"));

        verify(boardService).updateBoard(eq(1L), any(UpdateBoardRequest.class));
    }

    @Test
    void updateBoard_shouldHandlePartialUpdate() throws Exception {
        UpdateBoardRequest request = new UpdateBoardRequest();
        request.setName("Updated Name");
        // Description and visibility not set

        BoardResponse response = BoardResponse.builder()
                .id(1L)
                .name("Updated Name")
                .build();

        when(boardService.updateBoard(eq(1L), any(UpdateBoardRequest.class))).thenReturn(response);

        mockMvc()
                .perform(put("/boards/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));

        verify(boardService).updateBoard(eq(1L), any(UpdateBoardRequest.class));
    }

    @Test
    void updateBoard_shouldHandleInvalidJson() throws Exception {
        String invalidBody = "{ invalid }";

        mockMvc()
                .perform(put("/boards/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteBoard_shouldReturnNoContent() throws Exception {
        mockMvc()
                .perform(delete("/boards/1"))
                .andExpect(status().isNoContent());

        verify(boardService).deleteBoard(1L);
    }

    @Test
    void reorderBoards_shouldReturnReorderedBoards() throws Exception {
        ReorderBoardsRequest request = new ReorderBoardsRequest();
        request.setBoardIds(Arrays.asList(2L, 1L, 3L));

        BoardResponse board1 = BoardResponse.builder().id(1L).name("Board 1").build();
        BoardResponse board2 = BoardResponse.builder().id(2L).name("Board 2").build();
        BoardResponse board3 = BoardResponse.builder().id(3L).name("Board 3").build();
        List<BoardResponse> boards = Arrays.asList(board2, board1, board3);

        when(boardService.reorderBoards(eq(1L), any(ReorderBoardsRequest.class))).thenReturn(boards);

        mockMvc()
                .perform(patch("/boards/owner/1/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(2L))
                .andExpect(jsonPath("$[1].id").value(1L))
                .andExpect(jsonPath("$[2].id").value(3L));

        verify(boardService).reorderBoards(eq(1L), any(ReorderBoardsRequest.class));
    }

    @Test
    void reorderBoards_shouldHandleSingleBoardId() throws Exception {
        ReorderBoardsRequest request = new ReorderBoardsRequest();
        request.setBoardIds(Collections.singletonList(1L));

        BoardResponse board = BoardResponse.builder().id(1L).name("Board 1").build();
        when(boardService.reorderBoards(eq(1L), any(ReorderBoardsRequest.class)))
                .thenReturn(Collections.singletonList(board));

        mockMvc()
                .perform(patch("/boards/owner/1/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(boardService).reorderBoards(eq(1L), any(ReorderBoardsRequest.class));
    }

    @Test
    void reorderBoards_shouldHandleInvalidJson() throws Exception {
        String invalidBody = "{ invalid }";

        mockMvc()
                .perform(patch("/boards/owner/1/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchBoards_shouldHandleNullKeyword() throws Exception {
        SearchBoardsRequest request = new SearchBoardsRequest();
        request.setOwnerId(1L);
        request.setKeyword(null);

        BoardResponse board = BoardResponse.builder().id(1L).build();
        when(boardService.searchBoards(any(SearchBoardsRequest.class)))
                .thenReturn(Collections.singletonList(board));

        mockMvc()
                .perform(post("/boards/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(boardService).searchBoards(any(SearchBoardsRequest.class));
    }

    @Test
    void searchBoards_shouldHandleSorting() throws Exception {
        SearchBoardsRequest request = new SearchBoardsRequest();
        request.setOwnerId(1L);
        request.setKeyword("test");
        request.setSortBy("date");
        request.setSortOrder("desc");

        BoardResponse board = BoardResponse.builder().id(1L).build();
        when(boardService.searchBoards(any(SearchBoardsRequest.class)))
                .thenReturn(Collections.singletonList(board));

        mockMvc()
                .perform(post("/boards/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(boardService).searchBoards(any(SearchBoardsRequest.class));
    }
}
