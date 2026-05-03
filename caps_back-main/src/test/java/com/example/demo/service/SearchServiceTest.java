package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.example.demo.dto.search.SearchBoardResult;
import com.example.demo.dto.search.SearchPinResult;
import com.example.demo.dto.search.SearchResponse;
import com.example.demo.dto.search.SearchSort;
import com.example.demo.dto.search.SearchType;
import com.example.demo.entity.Board;
import com.example.demo.entity.Pin;
import com.example.demo.entity.PinMedia;
import com.example.demo.entity.UserAccount;
import com.example.demo.entity.enums.PinStatus;
import com.example.demo.exception.BusinessValidationException;
import com.example.demo.repository.BoardRepository;
import com.example.demo.repository.PinRepository;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private PinRepository pinRepository;

    @Mock
    private BoardRepository boardRepository;

    @InjectMocks
    private SearchService searchService;

    @Test
    void search_shouldThrow_whenQueryEmpty() {
        assertThatThrownBy(() -> searchService.search("   ", SearchType.ALL, SearchSort.RELEVANCE))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Query must contain at least one keyword");
    }

    @Test
    void search_shouldReturnPinsAndBoards_whenTypeAll() {
        Pin pin = buildPin(1L, "Cozy room", "soft minimal");
        Board board = buildBoard(2L, "Soft Minimal Interiors");

        when(pinRepository.searchPublishedPins("soft")).thenReturn(Collections.singletonList(pin));
        when(boardRepository.searchPublicBoards("soft")).thenReturn(Collections.singletonList(board));
        when(pinRepository.findLatestPinsForBoards(Collections.singletonList(2L), PinStatus.PUBLISHED))
                .thenReturn(Collections.singletonList(pin));

        SearchResponse response = searchService.search("soft", SearchType.ALL, SearchSort.RELEVANCE);

        assertThat(response.getPins()).hasSize(1);
        assertThat(response.getBoards()).hasSize(1);
    }

    @Test
    void search_shouldRespectTypePinsOnly() {
        Pin pin = buildPin(1L, "Cozy room", "soft minimal");
        when(pinRepository.searchPublishedPins("soft")).thenReturn(Collections.singletonList(pin));

        SearchResponse response = searchService.search("soft", SearchType.PINS, SearchSort.RELEVANCE);

        assertThat(response.getPins()).hasSize(1);
        assertThat(response.getBoards()).isEmpty();
    }

    @Test
    void search_shouldRespectTypeBoardsOnly() {
        Board board = buildBoard(2L, "Soft Minimal Interiors");
        when(boardRepository.searchPublicBoards("soft")).thenReturn(Collections.singletonList(board));
        when(pinRepository.findLatestPinsForBoards(Collections.singletonList(2L), PinStatus.PUBLISHED))
                .thenReturn(Collections.emptyList());

        SearchResponse response = searchService.search("soft", SearchType.BOARDS, SearchSort.RELEVANCE);

        assertThat(response.getPins()).isEmpty();
        assertThat(response.getBoards()).hasSize(1);
        SearchBoardResult result = response.getBoards().get(0);
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getName()).isEqualTo("Soft Minimal Interiors");
    }

    @Test
    void search_shouldSortPinsByRecentWhenRequested() {
        Pin older = buildPin(1L, "Soft bedroom", "soft");
        older.setCreatedAt(Instant.now().minusSeconds(120));

        Pin newer = buildPin(2L, "Soft living room", "soft");
        newer.setCreatedAt(Instant.now());

        when(pinRepository.searchPublishedPins("soft")).thenReturn(java.util.Arrays.asList(older, newer));

        SearchResponse response = searchService.search("soft", SearchType.PINS, SearchSort.RECENT);

        assertThat(response.getPins()).hasSize(2);
        assertThat(response.getPins().get(0).getId()).isEqualTo(2L);
        assertThat(response.getPins().get(1).getId()).isEqualTo(1L);
    }

    @Test
    void search_shouldSortBoardsByRecentWhenRequested() {
        Board older = buildBoard(1L, "Minimal Old Board");
        older.setCreatedAt(Instant.now().minusSeconds(300));

        Board newer = buildBoard(2L, "Minimal New Board");
        newer.setCreatedAt(Instant.now());

        when(boardRepository.searchPublicBoards("minimal"))
                .thenReturn(java.util.Arrays.asList(older, newer));

        SearchResponse response = searchService.search("minimal", SearchType.BOARDS, SearchSort.RECENT);

        assertThat(response.getBoards()).hasSize(2);
        assertThat(response.getBoards().get(0).getId()).isEqualTo(2L);
        assertThat(response.getBoards().get(1).getId()).isEqualTo(1L);
    }

    @Test
    void search_shouldLimitResultsToMaxResults() {
        // Build more than MAX_RESULTS pins (50) with matching keywords
        java.util.List<Pin> manyPins = new java.util.ArrayList<>();
        for (long i = 0; i < 60; i++) {
            manyPins.add(buildPin(i, "Soft pin " + i, "soft"));
        }
        when(pinRepository.searchPublishedPins("soft")).thenReturn(manyPins);

        SearchResponse response = searchService.search("soft", SearchType.PINS, SearchSort.RELEVANCE);

        assertThat(response.getPins().size()).isLessThanOrEqualTo(50);
    }

    @Test
    void search_shouldUseBoardCoverFromLatestPins() {
        Pin pin = buildPin(1L, "Soft room", "soft");
        Board board = pin.getBoard();
        board.setName("Soft Board");

        when(boardRepository.searchPublicBoards("soft")).thenReturn(Collections.singletonList(board));
        when(pinRepository.findLatestPinsForBoards(Collections.singletonList(2L), PinStatus.PUBLISHED))
                .thenReturn(Collections.singletonList(pin));

        SearchResponse response = searchService.search("soft", SearchType.BOARDS, SearchSort.RELEVANCE);

        assertThat(response.getBoards()).hasSize(1);
        assertThat(response.getBoards().get(0).getCoverUrl()).isEqualTo(pin.getMediaUrl());
    }

    @Test
    void search_shouldFilterOutPinsThatDoNotMatchAllTokens() {
        Pin matching = buildPin(1L, "Soft bedroom", "soft");
        Pin nonMatching = buildPin(2L, "Hard metal room", "metal");

        when(pinRepository.searchPublishedPins("soft")).thenReturn(java.util.Arrays.asList(matching, nonMatching));

        SearchResponse response = searchService.search("soft bedroom", SearchType.PINS, SearchSort.RELEVANCE);

        assertThat(response.getPins()).extracting(SearchPinResult::getId).containsExactly(1L);
    }

    @Test
    void search_shouldFilterOutBoardsThatDoNotMatchAllTokens() {
        Board matching = buildBoard(1L, "Soft Minimal Board");
        Board nonMatching = buildBoard(2L, "Hard Rock Board");

        when(boardRepository.searchPublicBoards("soft"))
                .thenReturn(java.util.Arrays.asList(matching, nonMatching));
        when(pinRepository.findLatestPinsForBoards(anyList(), eq(PinStatus.PUBLISHED)))
                .thenReturn(Collections.emptyList());

        SearchResponse response = searchService.search("soft minimal", SearchType.BOARDS, SearchSort.RELEVANCE);

        assertThat(response.getBoards()).extracting(SearchBoardResult::getId).containsExactly(1L);
    }

    @Test
    void search_shouldReturnEmptyBoards_whenNoCandidatesMatchTokens() {
        Board board = buildBoard(1L, "Completely Different");

        when(boardRepository.searchPublicBoards("soft")).thenReturn(Collections.singletonList(board));

        SearchResponse response = searchService.search("soft minimal", SearchType.BOARDS, SearchSort.RELEVANCE);

        assertThat(response.getBoards()).isEmpty();
    }

    @Test
    void search_shouldResolveThumbnailFromMediaItems_whenMediaUrlMissing() {
        UserAccount owner = new UserAccount();
        owner.setId(10L);
        owner.setUsername("owner");
        owner.setFullName("Owner Name");

        Board board = new Board();
        board.setId(2L);
        board.setName("Board Name");
        board.setOwner(owner);

        Pin pin = new Pin();
        pin.setId(1L);
        pin.setTitle("Soft pin");
        pin.setDescription("Description");
        pin.setOwner(owner);
        pin.setBoard(board);
        pin.setKeywords(new HashSet<>(Collections.singletonList("soft")));
        pin.setCreatedAt(Instant.now());
        pin.setMediaUrl(null);

        PinMedia media1 = new PinMedia();
        media1.setId(1L);
        media1.setUrl("");
        media1.setPosition(1);
        media1.setPin(pin);

        PinMedia media2 = new PinMedia();
        media2.setId(2L);
        media2.setUrl("thumb-url");
        media2.setPosition(0);
        media2.setPin(pin);

        pin.setMediaItems(java.util.Arrays.asList(media1, media2));

        when(pinRepository.searchPublishedPins("soft")).thenReturn(Collections.singletonList(pin));

        SearchResponse response = searchService.search("soft", SearchType.PINS, SearchSort.RELEVANCE);

        assertThat(response.getPins()).hasSize(1);
        assertThat(response.getPins().get(0).getThumbnailUrl()).isEqualTo("thumb-url");
    }

    private Pin buildPin(Long id, String title, String keyword) {
        UserAccount owner = new UserAccount();
        owner.setId(10L);
        owner.setUsername("owner");
        owner.setFullName("Owner Name");

        Board board = new Board();
        board.setId(2L);
        board.setName("Board Name");
        board.setOwner(owner);

        Pin pin = new Pin();
        pin.setId(id);
        pin.setTitle(title);
        pin.setDescription("Description");
        pin.setOwner(owner);
        pin.setBoard(board);
        pin.setKeywords(new HashSet<>(Collections.singletonList(keyword)));
        pin.setCreatedAt(Instant.now());
        return pin;
    }

    private Board buildBoard(Long id, String name) {
        UserAccount owner = new UserAccount();
        owner.setId(10L);
        owner.setUsername("owner");
        owner.setFullName("Owner Name");

        Board board = new Board();
        board.setId(id);
        board.setName(name);
        board.setOwner(owner);
        board.setCreatedAt(Instant.now());
        board.setUpdatedAt(Instant.now());
        return board;
    }
}


