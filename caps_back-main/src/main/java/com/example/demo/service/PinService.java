package com.example.demo.service;

import com.example.demo.dto.GlobalSearchRequest;
import com.example.demo.dto.GlobalSearchResponse;
import com.example.demo.dto.SearchSuggestionResponse;
import com.example.demo.dto.board.BoardResponse;
import com.example.demo.dto.board.CreateBoardRequest;
import com.example.demo.dto.pin.CreatePinRequest;
import com.example.demo.dto.pin.MediaItemRequest;
import com.example.demo.dto.pin.PinEngagementResponse;
import com.example.demo.dto.pin.PinMediaResponse;
import com.example.demo.dto.pin.PinResponse;
import com.example.demo.dto.pin.SearchPinsRequest;
import com.example.demo.dto.pin.UpdatePinRequest;
import com.example.demo.entity.Board;
import com.example.demo.entity.Pin;
import com.example.demo.entity.PinLike;
import com.example.demo.entity.PinLikeId;
import com.example.demo.entity.PinMedia;
import com.example.demo.entity.UserAccount;
import com.example.demo.entity.enums.PinStatus;
import com.example.demo.exception.BusinessValidationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.PinLikeRepository;
import com.example.demo.repository.PinRepository;
import com.example.demo.repository.UserAccountRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class PinService {

    private final PinRepository pinRepository;
    private final UserAccountRepository userAccountRepository;
    private final BoardService boardService;
    private final PinLikeRepository pinLikeRepository;

    @Autowired
    public PinService(
            PinRepository pinRepository,
            UserAccountRepository userAccountRepository,
            BoardService boardService,
            PinLikeRepository pinLikeRepository) {
        this.pinRepository = pinRepository;
        this.userAccountRepository = userAccountRepository;
        this.boardService = boardService;
        this.pinLikeRepository = pinLikeRepository;
    }

    @Transactional
    public PinResponse createPin(CreatePinRequest request) {
        UserAccount owner = findOwner(request.getOwnerId());
        Board board = resolveBoard(request, owner);

        Pin pin = new Pin();
        pin.setOwner(owner);
        pin.setBoard(board);
        pin.setTitle(request.getTitle());
        pin.setDescription(request.getDescription());
        pin.setMediaType(request.getMediaType());
        pin.setMediaUrl(request.getMediaUrl());
        pin.setSourceUrl(request.getSourceUrl());
        pin.setAttribution(request.getAttribution());
        pin.setVisibility(request.getVisibility());
        pin.setStatus(request.getStatus());
        pin.setKeywords(normalizeKeywords(request.getKeywords()));
        pin.setPublishedAt(resolvePublishedAt(request.getStatus(), null));

        replaceMediaItems(pin, request.getMediaItems());

        Pin saved = pinRepository.save(pin);
        return mapToResponse(saved);
    }

    @Transactional
    public void deletePin(Long pinId, Long userId) {
        Pin pin = getPinEntity(pinId);
        validateOwner(pin.getOwner().getId(), userId, "Only the owner can delete this pin");
        pinRepository.delete(pin);
    }

    @Transactional(readOnly = true)
    public PinResponse getPinById(Long pinId, Long userId) {
        Pin pin = getPinEntity(pinId);
        return mapToResponse(pin, userId);
    }

    @Transactional(readOnly = true)
    public List<PinResponse> getPinsForOwner(Long ownerId) {
        return pinRepository.findByOwnerId(ownerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PinResponse> getPinsByBoard(Long boardId) {
        return pinRepository.findByBoardId(boardId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PinResponse> searchPins(SearchPinsRequest request) {
        List<Pin> pins = pinRepository.searchPins(
                request.getOwnerId(),
                request.getKeyword(),
                request.getStatus(),
                request.getVisibility());

        List<PinResponse> responses = pins.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        // Apply sorting
        if (request.getSortBy() != null && request.getSortOrder() != null) {
            responses = sortPins(responses, request.getSortBy(), request.getSortOrder());
        }

        return responses;
    }

    @Transactional(readOnly = true)
    public List<PinResponse> getHomeFeedPins(Long viewerId) {
        List<Pin> feedPins = new ArrayList<>(pinRepository.findPublishedPublicPins());

        if (viewerId != null) {
            List<Pin> viewerPins = pinRepository.findByOwnerId(viewerId);
            Set<Long> knownIds =
                    feedPins.stream().map(Pin::getId).collect(Collectors.toCollection(LinkedHashSet::new));
            for (Pin pin : viewerPins) {
                if (knownIds.add(pin.getId())) {
                    feedPins.add(pin);
                }
            }
        }

        return feedPins.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public PinEngagementResponse incrementSaveCount(Long pinId) {
        Pin pin = getPinEntity(pinId);
        pin.setSaveCount(pin.getSaveCount() + 1);
        Pin saved = pinRepository.save(pin);
        return PinEngagementResponse.builder()
                .pinId(saved.getId())
                .saveCount(saved.getSaveCount())
                .shareCount(saved.getShareCount())
                .likeCount(saved.getLikeCount())
                .build();
    }

    @Transactional
    public PinResponse updatePin(Long pinId, UpdatePinRequest request) {
        Pin pin = getPinEntity(pinId);
        validateOwner(pin.getOwner().getId(), request.getOwnerId(), "Only the owner can edit this pin");

        boolean boardChangeRequested = Boolean.TRUE.equals(request.getDetachFromBoard())
                || request.getBoardId() != null
                || StringUtils.hasText(request.getNewBoardName());

        if (boardChangeRequested) {
            if (Boolean.TRUE.equals(request.getDetachFromBoard())) {
                pin.setBoard(null);
            } else {
                Board board = resolveBoardForUpdate(request, pin.getOwner());
                if (board != null) {
                    pin.setBoard(board);
                }
            }
        }

        if (request.getTitle() != null) {
            pin.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            pin.setDescription(request.getDescription());
        }
        if (request.getMediaType() != null) {
            pin.setMediaType(request.getMediaType());
        }
        if (request.getMediaUrl() != null) {
            pin.setMediaUrl(request.getMediaUrl());
        }
        if (request.getSourceUrl() != null) {
            pin.setSourceUrl(request.getSourceUrl());
        }
        if (request.getAttribution() != null) {
            pin.setAttribution(request.getAttribution());
        }
        if (request.getVisibility() != null) {
            pin.setVisibility(request.getVisibility());
        }
        if (request.getStatus() != null) {
            pin.setStatus(request.getStatus());
        }
        if (request.getKeywords() != null) {
            pin.setKeywords(normalizeKeywords(request.getKeywords()));
        }
        if (request.getMediaItems() != null) {
            replaceMediaItems(pin, request.getMediaItems());
        }

        pin.setPublishedAt(resolvePublishedAt(pin.getStatus(), pin.getPublishedAt()));

        Pin saved = pinRepository.save(pin);
        return mapToResponse(saved);
    }

    @Transactional
    public PinEngagementResponse incrementShareCount(Long pinId) {
        Pin pin = getPinEntity(pinId);
        pin.setShareCount(pin.getShareCount() + 1);
        Pin saved = pinRepository.save(pin);
        return PinEngagementResponse.builder()
                .pinId(saved.getId())
                .saveCount(saved.getSaveCount())
                .shareCount(saved.getShareCount())
                .likeCount(saved.getLikeCount())
                .build();
    }

    @Transactional
    public PinEngagementResponse toggleLike(Long pinId, Long userId) {
        Pin pin = getPinEntity(pinId);
        UserAccount user = userAccountRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        boolean alreadyLiked = pinLikeRepository.existsByPinIdAndUserId(pinId, userId);
        boolean isLiked;

        if (alreadyLiked) {
            // Unlike: remove the like
            pinLikeRepository.deleteByPinIdAndUserId(pinId, userId);
            isLiked = false;
        } else {
            // Like: add the like (database will enforce unique constraint via primary key)
            try {
                PinLike pinLike = new PinLike();
                PinLikeId likeId = new PinLikeId(pinId, userId);
                pinLike.setId(likeId);
                pinLike.setPin(pin);
                pinLike.setUser(user);
                pinLikeRepository.save(pinLike);
                isLiked = true;
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                // Handle race condition: if like was added between check and save
                // Re-check the actual state from database
                isLiked = pinLikeRepository.existsByPinIdAndUserId(pinId, userId);
            }
        }

        // Update like count from actual count in pin_likes table
        Long actualLikeCount = pinLikeRepository.countByPinId(pinId);
        pin.setLikeCount(actualLikeCount);
        Pin saved = pinRepository.save(pin);
        
        return PinEngagementResponse.builder()
                .pinId(saved.getId())
                .saveCount(saved.getSaveCount())
                .shareCount(saved.getShareCount())
                .likeCount(saved.getLikeCount())
                .isLiked(isLiked)
                .build();
    }

    private List<PinResponse> sortPins(List<PinResponse> pins, String sortBy, String sortOrder) {
        boolean ascending = "asc".equalsIgnoreCase(sortOrder);
        return pins.stream()
                .sorted((p1, p2) -> {
                    int comparison = 0;
                    switch (sortBy.toLowerCase()) {
                        case "date":
                            comparison = p1.getCreatedAt().compareTo(p2.getCreatedAt());
                            break;
                        case "title":
                            comparison = p1.getTitle().compareToIgnoreCase(p2.getTitle());
                            break;
                        default:
                            return 0;
                    }
                    return ascending ? comparison : -comparison;
                })
                .collect(Collectors.toList());
    }

    public Pin getPinEntity(Long pinId) {
        return pinRepository
                .findById(pinId)
                .orElseThrow(() -> new ResourceNotFoundException("Pin not found"));
    }

    private UserAccount findOwner(Long ownerId) {
        return userAccountRepository
                .findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));
    }

    private Board resolveBoard(CreatePinRequest request, UserAccount owner) {
        Board board = null;
        if (request.getBoardId() != null) {
            board = boardService.getBoardEntity(request.getBoardId());
            validateOwner(owner.getId(), board.getOwner().getId(), "Board does not belong to owner");
        } else if (StringUtils.hasText(request.getNewBoardName())) {
            CreateBoardRequest createBoardRequest = new CreateBoardRequest();
            createBoardRequest.setOwnerId(owner.getId());
            createBoardRequest.setName(request.getNewBoardName());
            createBoardRequest.setDescription(request.getNewBoardDescription());
            createBoardRequest.setVisibility(request.getNewBoardVisibility());
            board = boardService.createBoardEntity(createBoardRequest);
        }
        return board;
    }

    private Board resolveBoardForUpdate(UpdatePinRequest request, UserAccount owner) {
        if (request.getBoardId() != null) {
            Board board = boardService.getBoardEntity(request.getBoardId());
            validateOwner(owner.getId(), board.getOwner().getId(), "Board does not belong to owner");
            return board;
        } else if (StringUtils.hasText(request.getNewBoardName())) {
            CreateBoardRequest createBoardRequest = new CreateBoardRequest();
            createBoardRequest.setOwnerId(owner.getId());
            createBoardRequest.setName(request.getNewBoardName());
            createBoardRequest.setDescription(request.getNewBoardDescription());
            createBoardRequest.setVisibility(request.getNewBoardVisibility());
            return boardService.createBoardEntity(createBoardRequest);
        }
        return null;
    }

    private void validateOwner(Long expectedOwnerId, Long actualOwnerId, String message) {
        if (expectedOwnerId == null || actualOwnerId == null) {
            throw new BusinessValidationException(message);
        }
        if (!expectedOwnerId.equals(actualOwnerId)) {
            throw new BusinessValidationException(message);
        }
    }

    private void replaceMediaItems(Pin pin, List<MediaItemRequest> mediaRequests) {
        if (mediaRequests == null) {
            return;
        }
        pin.clearMediaItems();
        List<PinMedia> mediaItems = new ArrayList<>();
        for (MediaItemRequest mediaRequest : mediaRequests) {
            PinMedia media = new PinMedia();
            media.setUrl(mediaRequest.getUrl());
            media.setMediaType(mediaRequest.getMediaType());
            media.setPosition(mediaRequest.getPosition());
            media.setPin(pin);
            mediaItems.add(media);
        }
        pin.getMediaItems().addAll(mediaItems);
    }

    private Set<String> normalizeKeywords(Set<String> keywords) {
        if (keywords == null) {
            return new LinkedHashSet<>();
        }
        return keywords.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(k -> k.toLowerCase().replaceAll("\\s+", "-"))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Instant resolvePublishedAt(PinStatus status, Instant existingPublishedAt) {
        if (status == PinStatus.PUBLISHED) {
            return existingPublishedAt != null ? existingPublishedAt : Instant.now();
        }
        return null;
    }

    @Transactional(readOnly = true)
    public GlobalSearchResponse globalSearch(GlobalSearchRequest request) {
        if (request.getKeyword() == null || request.getKeyword().trim().isEmpty()) {
            return GlobalSearchResponse.builder()
                    .pins(new ArrayList<>())
                    .boards(new ArrayList<>())
                    .totalResults(0L)
                    .query(request.getKeyword())
                    .build();
        }

        String keyword = request.getKeyword().trim();
        String typeFilter = request.getTypeFilter() != null ? request.getTypeFilter().toLowerCase() : "all";

        List<PinResponse> pinResults = new ArrayList<>();
        List<BoardResponse> boardResults = new ArrayList<>();

        // Search pins if typeFilter is "all" or "pins"
        if ("all".equals(typeFilter) || "pins".equals(typeFilter)) {
            List<Pin> pins = pinRepository.searchPublicPins(keyword);
            pinResults = pins.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            // Apply sorting
            if (request.getSortBy() != null) {
                pinResults = sortGlobalPins(pinResults, request.getSortBy(), request.getSortOrder());
            }
        }

        // Search boards if typeFilter is "all" or "boards"
        if ("all".equals(typeFilter) || "boards".equals(typeFilter)) {
            List<Board> boards = boardService.searchPublicBoards(keyword);
            boardResults = new ArrayList<>();
            for (Board board : boards) {
                boardResults.add(boardService.mapToResponse(board));
            }

            // Apply sorting
            if (request.getSortBy() != null && request.getSortOrder() != null) {
                boardResults = boardService.sortBoards(boardResults, request.getSortBy(), request.getSortOrder());
            }
        }

        long totalResults = pinResults.size() + boardResults.size();

        return GlobalSearchResponse.builder()
                .pins(pinResults)
                .boards(boardResults)
                .totalResults(totalResults)
                .query(keyword)
                .build();
    }

    @Transactional(readOnly = true)
    public List<SearchSuggestionResponse> getSearchSuggestions(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String searchTerm = keyword.trim().toLowerCase();
        List<SearchSuggestionResponse> suggestions = new ArrayList<>();

        // Get pin suggestions (limit 5)
        List<Pin> pinSuggestions = pinRepository.searchPublicPinsWithSuggestions(searchTerm);
        suggestions.addAll(
                pinSuggestions.stream()
                        .limit(5)
                        .map(pin -> SearchSuggestionResponse.builder()
                                .id("pin-" + pin.getId())
                                .label(pin.getTitle())
                                .type("Pin")
                                .thumbnail(pin.getMediaUrl())
                                .subtitle(pin.getOwner().getUsername())
                                .pinId(pin.getId().toString())
                                .build())
                        .collect(Collectors.toList()));

        // Get board suggestions (limit 3)
        List<Board> boardSuggestions = boardService.searchPublicBoardsWithSuggestions(searchTerm);
        suggestions.addAll(
                boardSuggestions.stream()
                        .limit(3)
                        .map(board -> SearchSuggestionResponse.builder()
                                .id("board-" + board.getId())
                                .label(board.getName())
                                .type("Board")
                                .subtitle(board.getDescription() != null
                                        ? board.getDescription().substring(0, Math.min(50, board.getDescription().length()))
                                        : "")
                                .boardId(board.getId().toString())
                                .build())
                        .collect(Collectors.toList()));

        // Get keyword suggestions from existing pins
        Set<String> keywordSet = new LinkedHashSet<>();
        pinSuggestions.forEach(pin -> {
            if (pin.getKeywords() != null) {
                pin.getKeywords().stream()
                        .filter(k -> k.toLowerCase().contains(searchTerm))
                        .limit(2)
                        .forEach(keywordSet::add);
            }
        });
        suggestions.addAll(
                keywordSet.stream()
                        .limit(3)
                        .map(kw -> SearchSuggestionResponse.builder()
                                .id("keyword-" + kw)
                                .label(kw)
                                .type("Keyword")
                                .build())
                        .collect(Collectors.toList()));

        return suggestions.stream().limit(10).collect(Collectors.toList());
    }

    private List<PinResponse> sortGlobalPins(List<PinResponse> pins, String sortBy, String sortOrder) {
        boolean ascending = "asc".equalsIgnoreCase(sortOrder);
        return pins.stream()
                .sorted((p1, p2) -> {
                    int comparison = 0;
                    switch (sortBy.toLowerCase()) {
                        case "relevance":
                            // Exact title matches first, then by creation date
                            boolean p1Exact = p1.getTitle().toLowerCase().startsWith(
                                    p1.getTitle().toLowerCase().split("\\s+")[0]);
                            boolean p2Exact = p2.getTitle().toLowerCase().startsWith(
                                    p2.getTitle().toLowerCase().split("\\s+")[0]);
                            if (p1Exact != p2Exact) {
                                comparison = p1Exact ? -1 : 1;
                            } else {
                                comparison = p1.getCreatedAt().compareTo(p2.getCreatedAt());
                            }
                            break;
                        case "recent":
                            comparison = p1.getCreatedAt().compareTo(p2.getCreatedAt());
                            break;
                        case "popularity":
                            // For now, use creation date as popularity proxy
                            // In future, can add saves/shares count
                            comparison = p1.getCreatedAt().compareTo(p2.getCreatedAt());
                            break;
                        default:
                            return 0;
                    }
                    return ascending ? comparison : -comparison;
                })
                .collect(Collectors.toList());
    }

    private PinResponse mapToResponse(Pin pin) {
        return mapToResponse(pin, null);
    }

    private PinResponse mapToResponse(Pin pin, Long userId) {
        Boolean isLiked = null;
        if (userId != null) {
            isLiked = pinLikeRepository.existsByPinIdAndUserId(pin.getId(), userId);
        }
        
        return PinResponse.builder()
                .id(pin.getId())
                .ownerId(pin.getOwner().getId())
                .ownerUsername(pin.getOwner().getUsername())
                .ownerFullName(pin.getOwner().getFullName())
                .boardId(pin.getBoard() != null ? pin.getBoard().getId() : null)
                .boardName(pin.getBoard() != null ? pin.getBoard().getName() : null)
                .title(pin.getTitle())
                .description(pin.getDescription())
                .mediaType(pin.getMediaType())
                .mediaUrl(pin.getMediaUrl())
                .sourceUrl(pin.getSourceUrl())
                .attribution(pin.getAttribution())
                .keywords(pin.getKeywords())
                .visibility(pin.getVisibility())
                .status(pin.getStatus())
                .saveCount(pin.getSaveCount())
                .shareCount(pin.getShareCount())
                .likeCount(pin.getLikeCount())
                .isLiked(isLiked)
                .publishedAt(pin.getPublishedAt())
                .createdAt(pin.getCreatedAt())
                .updatedAt(pin.getUpdatedAt())
                .mediaItems(
                        pin.getMediaItems().stream()
                                .map(
                                        media ->
                                                PinMediaResponse.builder()
                                                        .id(media.getId())
                                                        .url(media.getUrl())
                                                        .mediaType(media.getMediaType())
                                                        .position(media.getPosition())
                                                        .build())
                                .collect(Collectors.toList()))
                .build();
    }
}
