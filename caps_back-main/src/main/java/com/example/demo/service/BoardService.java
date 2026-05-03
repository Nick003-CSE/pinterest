package com.example.demo.service;

import com.example.demo.dto.board.BoardResponse;
import com.example.demo.dto.board.CollaboratorInfo;
import com.example.demo.dto.board.CreateBoardRequest;
import com.example.demo.dto.board.ReorderBoardsRequest;
import com.example.demo.dto.board.SearchBoardsRequest;
import com.example.demo.dto.board.UpdateBoardRequest;
import com.example.demo.entity.Board;
import com.example.demo.entity.BoardCollaboration;
import com.example.demo.entity.UserAccount;
import com.example.demo.exception.BusinessValidationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BoardCollaborationRepository;
import com.example.demo.repository.BoardRepository;
import com.example.demo.repository.PinRepository;
import com.example.demo.repository.UserAccountRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserAccountRepository userAccountRepository;
    private final PinRepository pinRepository;
    private final BoardCollaborationRepository boardCollaborationRepository;

    @Autowired
    public BoardService(
            BoardRepository boardRepository,
            UserAccountRepository userAccountRepository,
            PinRepository pinRepository,
            BoardCollaborationRepository boardCollaborationRepository) {
        this.boardRepository = boardRepository;
        this.userAccountRepository = userAccountRepository;
        this.pinRepository = pinRepository;
        this.boardCollaborationRepository = boardCollaborationRepository;
    }

    public BoardResponse createBoard(CreateBoardRequest request) {
        Board saved = createBoardEntity(request);
        return mapToResponse(saved);
    }

    public Board createBoardEntity(CreateBoardRequest request) {
        UserAccount owner =
                userAccountRepository
                        .findById(request.getOwnerId())
                        .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));

        // Prevent duplicate board names per owner (case-insensitive)
        if (boardRepository.existsByOwnerIdAndNameIgnoreCase(owner.getId(), request.getName())) {
            throw new BusinessValidationException("A board with this name already exists for this user");
        }

        Board board = new Board();
        board.setOwner(owner);
        board.setName(request.getName());
        board.setDescription(request.getDescription());
        board.setVisibility(request.getVisibility());

        Board savedBoard = boardRepository.save(board);

        // if this board was created alongside a new pin, increment its pin count later
        // no direct pinCount column, but returning entity allows service to detect empties
        return savedBoard;
    }

    @Transactional(readOnly = true)
    public BoardResponse getBoardById(Long boardId) {
        Board board = getBoardEntity(boardId);
        return mapToResponse(board);
    }

    @Transactional(readOnly = true)
    public List<BoardResponse> getBoardsForOwner(Long ownerId) {
        List<Board> boards = new ArrayList<>(boardRepository.findByOwnerId(ownerId));

        // Include boards where the user is a collaborator
        List<BoardCollaboration> collaborations =
                boardCollaborationRepository.findByCollaboratorId(ownerId);
        for (BoardCollaboration collab : collaborations) {
            Board board = collab.getBoard();
            if (board != null
                    && boards.stream().noneMatch(b -> b.getId().equals(board.getId()))) {
                boards.add(board);
            }
        }

        return boards.stream()
                .sorted((b1, b2) -> Integer.compare(b1.getPosition(), b2.getPosition()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BoardResponse> searchBoards(SearchBoardsRequest request) {
        List<Board> boards = boardRepository.searchBoards(request.getOwnerId(), request.getKeyword());

        List<BoardResponse> responses = boards.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        // Apply sorting
        if (request.getSortBy() != null && request.getSortOrder() != null) {
            responses = sortBoards(responses, request.getSortBy(), request.getSortOrder());
        } else {
            // Default: sort by position
            responses = boards.stream()
                    .sorted((b1, b2) -> Integer.compare(b1.getPosition(), b2.getPosition()))
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        return responses;
    }

    @Transactional(readOnly = true)
    public List<Board> searchPublicBoards(String keyword) {
        return boardRepository.searchPublicBoards(keyword);
    }

    @Transactional(readOnly = true)
    public List<Board> searchPublicBoardsWithSuggestions(String keyword) {
        return boardRepository.searchPublicBoardsWithSuggestions(keyword);
    }

    public List<BoardResponse> sortBoards(List<BoardResponse> boards, String sortBy, String sortOrder) {
        boolean ascending = "asc".equalsIgnoreCase(sortOrder);
        return boards.stream()
                .sorted((b1, b2) -> {
                    int comparison = 0;
                    switch (sortBy.toLowerCase()) {
                        case "date":
                        case "recent":
                            comparison = b1.getCreatedAt().compareTo(b2.getCreatedAt());
                            break;
                        case "name":
                            comparison = b1.getName().compareToIgnoreCase(b2.getName());
                            break;
                        default:
                            return 0;
                    }
                    return ascending ? comparison : -comparison;
                })
                .collect(Collectors.toList());
    }

    public BoardResponse mapToResponse(Board board) {
        long pinCount = pinRepository.countByBoardId(board.getId());
        
        // Fetch collaborators for this board
        List<BoardCollaboration> collaborations = boardCollaborationRepository.findByBoardId(board.getId());
        List<CollaboratorInfo> collaborators = collaborations.stream()
                .map(collab -> {
                    var user = collab.getCollaborator();
                    String avatarUrl = "https://ui-avatars.com/api/?background=0f172a&color=fff&name="
                            + java.net.URLEncoder.encode(user.getFullName(), java.nio.charset.StandardCharsets.UTF_8);
                    return CollaboratorInfo.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .fullName(user.getFullName())
                            .avatarUrl(avatarUrl)
                            .build();
                })
                .collect(Collectors.toList());
        
        return BoardResponse.builder()
                .id(board.getId())
                .ownerId(board.getOwner().getId())
                .name(board.getName())
                .description(board.getDescription())
                .visibility(board.getVisibility())
                .pinCount(pinCount)
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .collaborators(collaborators)
                .build();
    }

    @Transactional
    public BoardResponse updateBoard(Long boardId, UpdateBoardRequest request) {
        Board board = getBoardEntity(boardId);

        if (request.getName() != null) {
            board.setName(request.getName());
        }
        if (request.getDescription() != null) {
            board.setDescription(request.getDescription());
        }
        if (request.getVisibility() != null) {
            board.setVisibility(request.getVisibility());
        }

        Board updated = boardRepository.save(board);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteBoard(Long boardId) {
        Board board = getBoardEntity(boardId);
        // Delete all pins in the board
        pinRepository.findByBoardId(boardId).forEach(pinRepository::delete);
        boardRepository.delete(board);
    }

    @Transactional
    public List<BoardResponse> reorderBoards(Long ownerId, ReorderBoardsRequest request) {
        List<Long> boardIds = request.getBoardIds();
        List<Board> boards = boardRepository.findAllById(boardIds);

        // Validate all boards belong to the owner
        for (Board board : boards) {
            if (!board.getOwner().getId().equals(ownerId)) {
                throw new BusinessValidationException("Board does not belong to owner");
            }
        }

        // Update positions
        for (int i = 0; i < boardIds.size(); i++) {
            Long boardId = boardIds.get(i);
            Board board = boards.stream()
                    .filter(b -> b.getId().equals(boardId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Board not found: " + boardId));
            board.setPosition(i);
        }

        boardRepository.saveAll(boards);

        return getBoardsForOwner(ownerId);
    }

    public Board getBoardEntity(Long boardId) {
        return boardRepository
                .findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));
    }
}

