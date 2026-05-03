package com.example.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.dto.GlobalSearchRequest;
import com.example.demo.dto.GlobalSearchResponse;
import com.example.demo.dto.SearchSuggestionResponse;
import com.example.demo.dto.pin.CreatePinRequest;
import com.example.demo.dto.pin.PinEngagementResponse;
import com.example.demo.dto.pin.PinResponse;
import com.example.demo.dto.pin.SearchPinsRequest;
import com.example.demo.dto.pin.UpdatePinRequest;
import com.example.demo.service.PinService;
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
class PinControllerTest {

    @Mock
    private PinService pinService;

    @InjectMocks
    private PinController pinController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(pinController).build();
    }

    @Test
    void getPinById_shouldReturnPinResponse() throws Exception {
        PinResponse response = PinResponse.builder()
                .id(1L)
                .title("Test Pin")
                .description("Desc")
                .createdAt(Instant.now())
                .build();

        when(pinService.getPinById(1L, null)).thenReturn(response);

        mockMvc()
                .perform(get("/pins/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Pin"));

        verify(pinService).getPinById(1L, null);
    }

    @Test
    void getPinById_shouldPassUserId_whenProvided() throws Exception {
        PinResponse response = PinResponse.builder()
                .id(1L)
                .isLiked(true)
                .build();

        when(pinService.getPinById(1L, 5L)).thenReturn(response);

        mockMvc()
                .perform(get("/pins/1").param("userId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.isLiked").value(true));

        verify(pinService).getPinById(1L, 5L);
    }

    @Test
    void getHomeFeedPins_shouldPassViewerId() throws Exception {
        when(pinService.getHomeFeedPins(5L)).thenReturn(Collections.emptyList());

        mockMvc()
                .perform(get("/pins/feed").param("viewerId", "5"))
                .andExpect(status().isOk());

        verify(pinService).getHomeFeedPins(5L);
    }

    @Test
    void getHomeFeedPins_shouldPassNull_whenViewerIdNotProvided() throws Exception {
        when(pinService.getHomeFeedPins(null)).thenReturn(Collections.emptyList());

        mockMvc()
                .perform(get("/pins/feed"))
                .andExpect(status().isOk());

        verify(pinService).getHomeFeedPins(null);
    }

    @Test
    void getHomeFeedPins_shouldReturnListOfPins() throws Exception {
        PinResponse pin1 = PinResponse.builder().id(1L).title("Pin 1").build();
        PinResponse pin2 = PinResponse.builder().id(2L).title("Pin 2").build();

        when(pinService.getHomeFeedPins(null)).thenReturn(Arrays.asList(pin1, pin2));

        mockMvc()
                .perform(get("/pins/feed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(pinService).getHomeFeedPins(null);
    }

    @Test
    void getPinsForOwner_shouldReturnListOfPins() throws Exception {
        PinResponse pin1 = PinResponse.builder().id(1L).build();
        PinResponse pin2 = PinResponse.builder().id(2L).build();

        when(pinService.getPinsForOwner(10L)).thenReturn(Arrays.asList(pin1, pin2));

        mockMvc()
                .perform(get("/pins/owner/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(pinService).getPinsForOwner(10L);
    }

    @Test
    void getPinsByBoard_shouldReturnListOfPins() throws Exception {
        PinResponse pin1 = PinResponse.builder().id(1L).boardId(5L).build();
        PinResponse pin2 = PinResponse.builder().id(2L).boardId(5L).build();

        when(pinService.getPinsByBoard(5L)).thenReturn(Arrays.asList(pin1, pin2));

        mockMvc()
                .perform(get("/pins/board/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].boardId").value(5L))
                .andExpect(jsonPath("$[1].boardId").value(5L));

        verify(pinService).getPinsByBoard(5L);
    }

    @Test
    void incrementSave_shouldReturnUpdatedEngagement() throws Exception {
        PinEngagementResponse engagement = PinEngagementResponse.builder()
                .pinId(1L)
                .saveCount(10L)
                .shareCount(5L)
                .likeCount(3L)
                .build();

        when(pinService.incrementSaveCount(1L)).thenReturn(engagement);

        mockMvc()
                .perform(post("/pins/1/save"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pinId").value(1L))
                .andExpect(jsonPath("$.saveCount").value(10L))
                .andExpect(jsonPath("$.shareCount").value(5L))
                .andExpect(jsonPath("$.likeCount").value(3L));

        verify(pinService).incrementSaveCount(1L);
    }

    @Test
    void incrementShare_shouldReturnUpdatedEngagement() throws Exception {
        PinEngagementResponse engagement = PinEngagementResponse.builder()
                .pinId(1L)
                .saveCount(5L)
                .shareCount(8L)
                .likeCount(2L)
                .build();

        when(pinService.incrementShareCount(1L)).thenReturn(engagement);

        mockMvc()
                .perform(post("/pins/1/share"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pinId").value(1L))
                .andExpect(jsonPath("$.shareCount").value(8L));

        verify(pinService).incrementShareCount(1L);
    }

    @Test
    void toggleLike_shouldReturnBadRequest_whenUserIdMissing() throws Exception {
        mockMvc()
                .perform(post("/pins/1/like"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void toggleLike_shouldReturnEngagement_whenUserIdProvided() throws Exception {
        PinEngagementResponse engagement = PinEngagementResponse.builder()
                .pinId(1L)
                .likeCount(5L)
                .isLiked(true)
                .build();

        when(pinService.toggleLike(1L, 10L)).thenReturn(engagement);

        mockMvc()
                .perform(post("/pins/1/like").param("userId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pinId").value(1L))
                .andExpect(jsonPath("$.isLiked").value(true))
                .andExpect(jsonPath("$.likeCount").value(5L));

        verify(pinService).toggleLike(1L, 10L);
    }

    @Test
    void toggleLike_shouldReturnUnliked_whenAlreadyLiked() throws Exception {
        PinEngagementResponse engagement = PinEngagementResponse.builder()
                .pinId(1L)
                .likeCount(4L)
                .isLiked(false)
                .build();

        when(pinService.toggleLike(1L, 10L)).thenReturn(engagement);

        mockMvc()
                .perform(post("/pins/1/like").param("userId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isLiked").value(false));

        verify(pinService).toggleLike(1L, 10L);
    }

    @Test
    void updatePin_shouldCallServiceWithBody() throws Exception {
        PinResponse response = PinResponse.builder()
                .id(1L)
                .title("Updated Title")
                .description("Updated Desc")
                .build();

        when(pinService.updatePin(eq(1L), any())).thenReturn(response);

        String body = """
                {
                  "ownerId": 10,
                  "title": "Updated Title",
                  "description": "Updated Desc"
                }
                """;

        mockMvc()
                .perform(put("/pins/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.description").value("Updated Desc"));

        verify(pinService).updatePin(eq(1L), any());
    }

    @Test
    void updatePin_shouldHandleInvalidJson() throws Exception {
        String invalidBody = "{ invalid json }";

        mockMvc()
                .perform(put("/pins/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deletePin_shouldReturnNoContent() throws Exception {
        mockMvc()
                .perform(delete("/pins/1").param("userId", "123"))
                .andExpect(status().isNoContent());

        verify(pinService).deletePin(1L, 123L);
    }

    @Test
    void deletePin_shouldReturnBadRequest_whenUserIdMissing() throws Exception {
        mockMvc()
                .perform(delete("/pins/1"))
                .andExpect(status().isBadRequest());

        verify(pinService, never()).deletePin(anyLong(), anyLong());
    }

    @Test
    void searchPins_shouldReturnFilteredPins() throws Exception {
        SearchPinsRequest request = new SearchPinsRequest();
        request.setOwnerId(10L);
        request.setKeyword("test");

        PinResponse pin = PinResponse.builder().id(1L).title("Test Pin").build();
        when(pinService.searchPins(any(SearchPinsRequest.class))).thenReturn(Collections.singletonList(pin));

        mockMvc()
                .perform(post("/pins/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(pinService).searchPins(any(SearchPinsRequest.class));
    }

    @Test
    void globalSearch_shouldReturnSearchResults() throws Exception {
        GlobalSearchRequest request = new GlobalSearchRequest();
        request.setKeyword("test");
        request.setTypeFilter("all");

        GlobalSearchResponse response = GlobalSearchResponse.builder()
                .query("test")
                .totalResults(5L)
                .pins(Collections.emptyList())
                .boards(Collections.emptyList())
                .build();

        when(pinService.globalSearch(any(GlobalSearchRequest.class))).thenReturn(response);

        mockMvc()
                .perform(post("/pins/global-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value("test"))
                .andExpect(jsonPath("$.totalResults").value(5L));

        verify(pinService).globalSearch(any(GlobalSearchRequest.class));
    }

    @Test
    void getSearchSuggestions_shouldReturnSuggestions() throws Exception {
        SearchSuggestionResponse suggestion1 = SearchSuggestionResponse.builder()
                .id("pin-1")
                .label("Test Pin")
                .type("Pin")
                .build();
        SearchSuggestionResponse suggestion2 = SearchSuggestionResponse.builder()
                .id("board-1")
                .label("Test Board")
                .type("Board")
                .build();

        when(pinService.getSearchSuggestions("test")).thenReturn(Arrays.asList(suggestion1, suggestion2));

        mockMvc()
                .perform(get("/pins/suggestions").param("keyword", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].type").value("Pin"))
                .andExpect(jsonPath("$[1].type").value("Board"));

        verify(pinService).getSearchSuggestions("test");
    }

    @Test
    void getSearchSuggestions_shouldReturnEmpty_whenKeywordNotProvided() throws Exception {
        when(pinService.getSearchSuggestions(null)).thenReturn(Collections.emptyList());

        mockMvc()
                .perform(get("/pins/suggestions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(pinService).getSearchSuggestions(null);
    }

    @Test
    void getSearchSuggestions_shouldReturnEmpty_whenKeywordIsEmpty() throws Exception {
        when(pinService.getSearchSuggestions("")).thenReturn(Collections.emptyList());

        mockMvc()
                .perform(get("/pins/suggestions").param("keyword", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(pinService).getSearchSuggestions("");
    }
}
