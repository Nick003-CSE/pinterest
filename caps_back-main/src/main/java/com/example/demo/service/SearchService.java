package com.example.demo.service;

import com.example.demo.dto.search.SearchBoardResult;
import com.example.demo.dto.search.SearchPinResult;
import com.example.demo.dto.search.SearchResponse;
import com.example.demo.dto.search.SearchSort;
import com.example.demo.dto.search.SearchType;
import com.example.demo.entity.Board;
import com.example.demo.entity.Pin;
import com.example.demo.entity.PinMedia;
import com.example.demo.entity.enums.PinStatus;
import com.example.demo.exception.BusinessValidationException;
import com.example.demo.repository.BoardRepository;
import com.example.demo.repository.PinRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class SearchService {

    private static final int MAX_RESULTS = 50;

    private final PinRepository pinRepository;
    private final BoardRepository boardRepository;

    @Autowired
    public SearchService(PinRepository pinRepository, BoardRepository boardRepository) {
        this.pinRepository = pinRepository;
        this.boardRepository = boardRepository;
    }

    @Transactional(readOnly = true)
    public SearchResponse search(String rawQuery, SearchType type, SearchSort sort) {
        List<String> tokens = tokenize(rawQuery);
        if (tokens.isEmpty()) {
            throw new BusinessValidationException("Query must contain at least one keyword");
        }

        String primaryToken = tokens.get(0);

        List<SearchPinResult> pinResults =
                type.includesPins()
                        ? buildPinResults(tokens, sort, primaryToken)
                        : Collections.emptyList();

        List<SearchBoardResult> boardResults =
                type.includesBoards()
                        ? buildBoardResults(tokens, sort, primaryToken)
                        : Collections.emptyList();

        return SearchResponse.builder().pins(pinResults).boards(boardResults).build();
    }

    private List<SearchPinResult> buildPinResults(
            List<String> tokens, SearchSort sort, String primaryToken) {
        List<Pin> candidates = pinRepository.searchPublishedPins(primaryToken);

        Comparator<Pin> comparator = sort == SearchSort.RECENT
                ? Comparator.comparing(Pin::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed()
                : Comparator.<Pin>comparingInt(pin -> calculateScore(pin, tokens))
                        .reversed()
                        .thenComparing(
                                Pin::getCreatedAt,
                                Comparator.nullsLast(Comparator.reverseOrder()));

        return candidates.stream()
                .filter(pin -> matchesTokens(buildPinSearchBlob(pin), tokens))
                .sorted(comparator)
                .limit(MAX_RESULTS)
                .map(this::mapToPinResult)
                .collect(Collectors.toList());
    }

    private List<SearchBoardResult> buildBoardResults(
            List<String> tokens, SearchSort sort, String primaryToken) {
        List<Board> candidates = boardRepository.searchPublicBoards(primaryToken);
        Comparator<Board> comparator = sort == SearchSort.RECENT
                ? Comparator.comparing(Board::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed()
                : Comparator.<Board>comparingInt(board -> calculateScore(board, tokens))
                        .reversed()
                        .thenComparing(
                                Board::getCreatedAt,
                                Comparator.nullsLast(Comparator.reverseOrder()));

        List<Board> filteredBoards = candidates.stream()
                .filter(board -> matchesTokens(buildBoardSearchBlob(board), tokens))
                .sorted(comparator)
                .limit(MAX_RESULTS)
                .collect(Collectors.toList());

        Map<Long, String> coverMap = resolveBoardCoverUrls(filteredBoards);

        return filteredBoards.stream()
                .map(board -> mapToBoardResult(board, coverMap))
                .collect(Collectors.toList());
    }

    private Map<Long, String> resolveBoardCoverUrls(List<Board> boards) {
        if (CollectionUtils.isEmpty(boards)) {
            return Collections.emptyMap();
        }
        List<Long> boardIds = boards.stream().map(Board::getId).collect(Collectors.toList());

        List<Pin> recentPins = pinRepository.findLatestPinsForBoards(boardIds, PinStatus.PUBLISHED);
        Map<Long, String> coverUrls = new HashMap<>();
        for (Pin pin : recentPins) {
            if (pin.getBoard() == null || !StringUtils.hasText(pin.getMediaUrl())) {
                continue;
            }
            Long boardId = pin.getBoard().getId();
            coverUrls.putIfAbsent(boardId, pin.getMediaUrl());
        }
        return coverUrls;
    }

    private SearchPinResult mapToPinResult(Pin pin) {
        return SearchPinResult.builder()
                .id(pin.getId())
                .boardId(pin.getBoard() != null ? pin.getBoard().getId() : null)
                .boardName(pin.getBoard() != null ? pin.getBoard().getName() : null)
                .title(pin.getTitle())
                .description(pin.getDescription())
                .thumbnailUrl(resolveThumbnail(pin))
                .ownerName(pin.getOwner().getFullName())
                .ownerUsername(pin.getOwner().getUsername())
                .keywords(pin.getKeywords())
                .createdAt(pin.getCreatedAt())
                .build();
    }

    private SearchBoardResult mapToBoardResult(Board board, Map<Long, String> coverUrls) {
        return SearchBoardResult.builder()
                .id(board.getId())
                .name(board.getName())
                .description(board.getDescription())
                .ownerName(board.getOwner().getFullName())
                .ownerUsername(board.getOwner().getUsername())
                .coverUrl(coverUrls.get(board.getId()))
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
    }

    private String resolveThumbnail(Pin pin) {
        if (StringUtils.hasText(pin.getMediaUrl())) {
            return pin.getMediaUrl();
        }
        List<PinMedia> mediaItems = pin.getMediaItems();
        if (CollectionUtils.isEmpty(mediaItems)) {
            return null;
        }
        return mediaItems.stream()
                .sorted(
                        Comparator.comparing(
                                PinMedia::getPosition, Comparator.nullsLast(Integer::compareTo)))
                .map(PinMedia::getUrl)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);
    }

    private int calculateScore(Pin pin, List<String> tokens) {
        return calculateScore(buildPinSearchBlob(pin), tokens);
    }

    private int calculateScore(Board board, List<String> tokens) {
        return calculateScore(buildBoardSearchBlob(board), tokens);
    }

    private int calculateScore(String blob, List<String> tokens) {
        int score = 0;
        for (String token : tokens) {
            if (blob.contains(token)) {
                score++;
            }
        }
        return score;
    }

    private boolean matchesTokens(String blob, List<String> tokens) {
        for (String token : tokens) {
            if (!blob.contains(token)) {
                return false;
            }
        }
        return true;
    }

    private String buildPinSearchBlob(Pin pin) {
        List<String> parts = new ArrayList<>();
        parts.add(pin.getTitle());
        parts.add(pin.getDescription());
        parts.add(pin.getBoard() != null ? pin.getBoard().getName() : null);
        parts.add(pin.getOwner().getUsername());
        parts.add(pin.getOwner().getFullName());
        addKeywords(parts, pin.getKeywords());
        return normalize(parts);
    }

    private String buildBoardSearchBlob(Board board) {
        List<String> parts = new ArrayList<>();
        parts.add(board.getName());
        parts.add(board.getDescription());
        parts.add(board.getOwner().getUsername());
        parts.add(board.getOwner().getFullName());
        return normalize(parts);
    }

    private void addKeywords(List<String> parts, Set<String> keywords) {
        if (CollectionUtils.isEmpty(keywords)) {
            return;
        }
        parts.addAll(new LinkedList<>(keywords));
    }

    private String normalize(Collection<String> parts) {
        return parts.stream()
                .filter(StringUtils::hasText)
                .map(value -> value.toLowerCase(Locale.US))
                .collect(Collectors.joining(" "));
    }

    private List<String> tokenize(String rawQuery) {
        if (!StringUtils.hasText(rawQuery)) {
            return Collections.emptyList();
        }
        String normalized = rawQuery.trim().toLowerCase(Locale.US);
        String[] parts = normalized.split("\\s+");
        List<String> tokens = new ArrayList<>();
        for (String part : parts) {
            if (StringUtils.hasText(part)) {
                tokens.add(part);
            }
        }
        return tokens;
    }
}

