package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.dto.GlobalSearchRequest;
import com.example.demo.dto.GlobalSearchResponse;
import com.example.demo.dto.SearchSuggestionResponse;
import com.example.demo.dto.board.BoardResponse;
import com.example.demo.dto.board.CreateBoardRequest;
import com.example.demo.dto.pin.CreatePinRequest;
import com.example.demo.dto.pin.MediaItemRequest;
import com.example.demo.dto.pin.PinEngagementResponse;
import com.example.demo.dto.pin.PinResponse;
import com.example.demo.dto.pin.SearchPinsRequest;
import com.example.demo.dto.pin.UpdatePinRequest;
import com.example.demo.entity.Board;
import com.example.demo.entity.Pin;
import com.example.demo.entity.PinLike;
import com.example.demo.entity.PinMedia;
import com.example.demo.entity.UserAccount;
import com.example.demo.entity.enums.MediaType;
import com.example.demo.entity.enums.PinStatus;
import com.example.demo.entity.enums.PinVisibility;
import com.example.demo.exception.BusinessValidationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.PinLikeRepository;
import com.example.demo.repository.PinRepository;
import com.example.demo.repository.UserAccountRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PinServiceTest {

    @Mock
    private PinRepository pinRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private BoardService boardService;

    @Mock
    private PinLikeRepository pinLikeRepository;

    @InjectMocks
    private PinService pinService;

    private UserAccount buildOwner(Long id) {
        UserAccount owner = new UserAccount();
        owner.setId(id);
        owner.setUsername("owner" + id);
        owner.setFullName("Owner " + id);
        return owner;
    }

    private Pin buildPin(Long id, UserAccount owner) {
        Pin pin = new Pin();
        pin.setId(id);
        pin.setOwner(owner);
        pin.setTitle("Title " + id);
        pin.setDescription("Desc");
        pin.setMediaType(MediaType.IMAGE);
        pin.setMediaUrl("http://image");
        pin.setVisibility(PinVisibility.PUBLIC);
        pin.setStatus(PinStatus.PUBLISHED);
        pin.setCreatedAt(Instant.now());
        pin.setUpdatedAt(Instant.now());
        pin.setSaveCount(0L);
        pin.setShareCount(0L);
        pin.setLikeCount(0L);

        PinMedia media = new PinMedia();
        media.setId(1L);
        media.setUrl("http://image");
        media.setMediaType(MediaType.IMAGE);
        media.setPosition(0);
        media.setPin(pin);
        List<PinMedia> mediaItems = new ArrayList<>();
        mediaItems.add(media);
        pin.setMediaItems(mediaItems);

        return pin;
    }

    @Test
    void getPinEntity_shouldThrow_whenNotFound() {
        when(pinRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pinService.getPinEntity(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Pin not found");
    }

    @Test
    void createPin_shouldThrow_whenOwnerNotFound() {
        CreatePinRequest request = new CreatePinRequest();
        request.setOwnerId(1L);

        when(userAccountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pinService.createPin(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Owner not found");
    }

    @Test
    void createPin_shouldCreateWithExistingBoard_whenBoardIdProvided() {
        UserAccount owner = buildOwner(1L);
        Board board = new Board();
        board.setId(10L);
        board.setOwner(owner);

        CreatePinRequest request = new CreatePinRequest();
        request.setOwnerId(1L);
        request.setBoardId(10L);
        request.setTitle("My Pin");
        request.setDescription("Desc");
        request.setMediaType(MediaType.IMAGE);
        request.setMediaUrl("url");
        request.setVisibility(PinVisibility.PUBLIC);
        request.setStatus(PinStatus.PUBLISHED);
        request.setKeywords(new HashSet<>(Arrays.asList("Tag One", "tag two")));

        MediaItemRequest mediaItem = new MediaItemRequest();
        mediaItem.setUrl("url");
        mediaItem.setMediaType(MediaType.IMAGE);
        mediaItem.setPosition(0);
        request.setMediaItems(Collections.singletonList(mediaItem));

        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(boardService.getBoardEntity(10L)).thenReturn(board);

        // validateOwner inside resolveBoard calls board.getOwner().getId() vs owner.getId()
        when(pinRepository.save(any(Pin.class))).thenAnswer(invocation -> {
            Pin p = invocation.getArgument(0);
            p.setId(100L);
            return p;
        });

        PinResponse response = pinService.createPin(request);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getBoardId()).isEqualTo(10L);
        assertThat(response.getOwnerId()).isEqualTo(1L);
        assertThat(response.getKeywords()).contains("tag-one", "tag-two");
    }

    @Test
    void createPin_shouldCreateNewBoard_whenNewBoardNameProvided() {
        UserAccount owner = buildOwner(1L);
        Board newBoard = new Board();
        newBoard.setId(20L);
        newBoard.setOwner(owner);

        CreatePinRequest request = new CreatePinRequest();
        request.setOwnerId(1L);
        request.setNewBoardName("New Board");

        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(boardService.createBoardEntity(any(CreateBoardRequest.class))).thenReturn(newBoard);
        when(pinRepository.save(any(Pin.class))).thenAnswer(invocation -> {
            Pin p = invocation.getArgument(0);
            p.setId(200L);
            return p;
        });

        PinResponse response = pinService.createPin(request);

        assertThat(response.getBoardId()).isEqualTo(20L);
        verify(boardService).createBoardEntity(any(CreateBoardRequest.class));
    }

    @Test
    void deletePin_shouldDeleteExistingPin_whenUserIsOwner() {
        UserAccount owner = buildOwner(1L);
        Pin pin = buildPin(5L, owner);

        when(pinRepository.findById(5L)).thenReturn(Optional.of(pin));

        pinService.deletePin(5L, 1L);

        verify(pinRepository).delete(pin);
    }

    @Test
    void deletePin_shouldThrowException_whenUserIsNotOwner() {
        UserAccount owner = buildOwner(1L);
        Pin pin = buildPin(5L, owner);

        when(pinRepository.findById(5L)).thenReturn(Optional.of(pin));

        assertThatThrownBy(() -> pinService.deletePin(5L, 2L))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Only the owner can delete this pin");

        verify(pinRepository, never()).delete(any());
    }

    @Test
    void getPinById_shouldIncludeIsLikedFlag_whenUserProvided() {
        UserAccount owner = buildOwner(1L);
        Pin pin = buildPin(5L, owner);

        when(pinRepository.findById(5L)).thenReturn(Optional.of(pin));
        when(pinLikeRepository.existsByPinIdAndUserId(5L, 2L)).thenReturn(true);

        PinResponse response = pinService.getPinById(5L, 2L);

        assertThat(response.getIsLiked()).isTrue();
    }

    @Test
    void getPinsForOwner_shouldMapResponses() {
        UserAccount owner = buildOwner(1L);
        Pin pin = buildPin(1L, owner);

        when(pinRepository.findByOwnerId(1L)).thenReturn(Collections.singletonList(pin));

        List<PinResponse> responses = pinService.getPinsForOwner(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getOwnerId()).isEqualTo(1L);
    }

    @Test
    void getPinsByBoard_shouldMapResponses() {
        UserAccount owner = buildOwner(1L);
        Pin pin = buildPin(1L, owner);

        when(pinRepository.findByBoardId(10L)).thenReturn(Collections.singletonList(pin));

        List<PinResponse> responses = pinService.getPinsByBoard(10L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void searchPins_shouldApplySorting_whenSortParamsProvided() {
        UserAccount owner = buildOwner(1L);
        Pin older = buildPin(1L, owner);
        older.setTitle("Alpha");
        older.setCreatedAt(Instant.now().minusSeconds(60));

        Pin newer = buildPin(2L, owner);
        newer.setTitle("Beta");
        newer.setCreatedAt(Instant.now());

        SearchPinsRequest request = new SearchPinsRequest();
        request.setOwnerId(1L);
        request.setKeyword("kw");
        request.setSortBy("title");
        request.setSortOrder("asc");

        when(pinRepository.searchPins(eq(1L), eq("kw"), any(), any()))
                .thenReturn(Arrays.asList(newer, older));

        List<PinResponse> responses = pinService.searchPins(request);

        assertThat(responses).extracting(PinResponse::getTitle).containsExactly("Alpha", "Beta");
    }

    @Test
    void getHomeFeedPins_shouldIncludeViewerPinsWithoutDuplicates() {
        UserAccount owner = buildOwner(1L);
        Pin publicPin = buildPin(1L, owner);
        Pin viewerPin1 = buildPin(2L, owner);
        Pin viewerPin2 = buildPin(3L, owner);

        when(pinRepository.findPublishedPublicPins()).thenReturn(Collections.singletonList(publicPin));
        when(pinRepository.findByOwnerId(1L)).thenReturn(Arrays.asList(publicPin, viewerPin1, viewerPin2));

        List<PinResponse> feed = pinService.getHomeFeedPins(1L);

        assertThat(feed).hasSize(3);
        assertThat(new HashSet<>(Arrays.asList(
                         feed.get(0).getId(), feed.get(1).getId(), feed.get(2).getId())))
                .containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    @Test
    void incrementSaveCount_shouldIncreaseAndReturnCounts() {
        UserAccount owner = buildOwner(1L);
        Pin pin = buildPin(1L, owner);
        pin.setSaveCount(1L);

        when(pinRepository.findById(1L)).thenReturn(Optional.of(pin));
        when(pinRepository.save(any(Pin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PinEngagementResponse response = pinService.incrementSaveCount(1L);

        assertThat(response.getSaveCount()).isEqualTo(2L);
    }

    @Test
    void incrementShareCount_shouldIncreaseAndReturnCounts() {
        UserAccount owner = buildOwner(1L);
        Pin pin = buildPin(1L, owner);
        pin.setShareCount(5L);

        when(pinRepository.findById(1L)).thenReturn(Optional.of(pin));
        when(pinRepository.save(any(Pin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PinEngagementResponse response = pinService.incrementShareCount(1L);

        assertThat(response.getShareCount()).isEqualTo(6L);
    }

    @Test
    void updatePin_shouldThrow_whenOwnerMismatch() {
        UserAccount owner = buildOwner(1L);
        Pin pin = buildPin(1L, owner);

        UpdatePinRequest request = new UpdatePinRequest();
        request.setOwnerId(2L); // different from pin.owner.id

        when(pinRepository.findById(1L)).thenReturn(Optional.of(pin));

        assertThatThrownBy(() -> pinService.updatePin(1L, request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Only the owner can edit this pin");
    }

    @Test
    void updatePin_shouldDetachFromBoard_whenRequested() {
        UserAccount owner = buildOwner(1L);
        Board board = new Board();
        board.setId(10L);
        board.setOwner(owner);
        Pin pin = buildPin(1L, owner);
        pin.setBoard(board);

        UpdatePinRequest request = new UpdatePinRequest();
        request.setOwnerId(1L);
        request.setDetachFromBoard(true);

        when(pinRepository.findById(1L)).thenReturn(Optional.of(pin));
        when(pinRepository.save(any(Pin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PinResponse response = pinService.updatePin(1L, request);

        assertThat(response.getBoardId()).isNull();
    }

    @Test
    void toggleLike_shouldThrow_whenUserNotFound() {
        UserAccount owner = buildOwner(1L);
        Pin pin = buildPin(1L, owner);

        when(pinRepository.findById(1L)).thenReturn(Optional.of(pin));
        when(userAccountRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pinService.toggleLike(1L, 2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void toggleLike_shouldUnlike_whenAlreadyLiked() {
        UserAccount owner = buildOwner(1L);
        Pin pin = buildPin(1L, owner);

        when(pinRepository.findById(1L)).thenReturn(Optional.of(pin));
        when(userAccountRepository.findById(2L)).thenReturn(Optional.of(buildOwner(2L)));
        when(pinLikeRepository.existsByPinIdAndUserId(1L, 2L)).thenReturn(true);
        when(pinLikeRepository.countByPinId(1L)).thenReturn(0L);
        when(pinRepository.save(any(Pin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PinEngagementResponse response = pinService.toggleLike(1L, 2L);

        verify(pinLikeRepository).deleteByPinIdAndUserId(1L, 2L);
        assertThat(response.getIsLiked()).isFalse();
        assertThat(response.getLikeCount()).isEqualTo(0L);
    }

    @Test
    void toggleLike_shouldLike_whenNotAlreadyLiked() {
        UserAccount owner = buildOwner(1L);
        Pin pin = buildPin(1L, owner);

        when(pinRepository.findById(1L)).thenReturn(Optional.of(pin));
        when(userAccountRepository.findById(2L)).thenReturn(Optional.of(buildOwner(2L)));
        when(pinLikeRepository.existsByPinIdAndUserId(1L, 2L)).thenReturn(false);
        when(pinLikeRepository.countByPinId(1L)).thenReturn(1L);
        when(pinRepository.save(any(Pin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PinEngagementResponse response = pinService.toggleLike(1L, 2L);

        verify(pinLikeRepository).save(any(PinLike.class));
        assertThat(response.getIsLiked()).isTrue();
        assertThat(response.getLikeCount()).isEqualTo(1L);
    }

    @Test
    void globalSearch_shouldReturnEmpty_whenKeywordBlank() {
        GlobalSearchRequest request = new GlobalSearchRequest();
        request.setKeyword("   ");

        GlobalSearchResponse response = pinService.globalSearch(request);

        assertThat(response.getTotalResults()).isEqualTo(0L);
        assertThat(response.getPins()).isEmpty();
        assertThat(response.getBoards()).isEmpty();
    }

    @Test
    void globalSearch_shouldSearchPinsAndBoards_whenTypeAll() {
        GlobalSearchRequest request = new GlobalSearchRequest();
        request.setKeyword("test");
        request.setTypeFilter("all");

        UserAccount owner = buildOwner(1L);
        Pin pin = buildPin(1L, owner);

        Board board = new Board();
        board.setId(10L);
        board.setOwner(owner);
        board.setName("Board");
        board.setCreatedAt(Instant.now());
        board.setUpdatedAt(Instant.now());

        when(pinRepository.searchPublicPins("test")).thenReturn(Collections.singletonList(pin));
        when(boardService.searchPublicBoards("test")).thenReturn(Collections.singletonList(board));
        when(boardService.mapToResponse(board)).thenReturn(BoardResponse.builder().id(10L).name("Board").build());

        GlobalSearchResponse response = pinService.globalSearch(request);

        assertThat(response.getPins()).hasSize(1);
        assertThat(response.getBoards()).hasSize(1);
        assertThat(response.getTotalResults()).isEqualTo(2L);
    }

    @Test
    void getSearchSuggestions_shouldReturnEmpty_whenKeywordBlank() {
        List<SearchSuggestionResponse> suggestions = pinService.getSearchSuggestions("   ");
        assertThat(suggestions).isEmpty();
    }

    @Test
    void getSearchSuggestions_shouldIncludePinsBoardsAndKeywords() {
        String keyword = "fit";
        UserAccount owner = buildOwner(1L);

        Pin pin = buildPin(1L, owner);
        Set<String> keywords = new LinkedHashSet<>();
        keywords.add("fitness-tips");
        pin.setKeywords(keywords);

        Board board = new Board();
        board.setId(10L);
        board.setOwner(owner);
        board.setName("Fitness Board");
        board.setDescription("All about fitness and health");
        board.setCreatedAt(Instant.now());
        board.setUpdatedAt(Instant.now());

        when(pinRepository.searchPublicPinsWithSuggestions("fit")).thenReturn(Collections.singletonList(pin));
        when(boardService.searchPublicBoardsWithSuggestions("fit")).thenReturn(Collections.singletonList(board));

        List<SearchSuggestionResponse> suggestions = pinService.getSearchSuggestions(keyword);

        assertThat(suggestions).isNotEmpty();
        assertThat(suggestions.stream().map(SearchSuggestionResponse::getType))
                .containsAnyOf("Pin", "Board", "Keyword");
    }
}


