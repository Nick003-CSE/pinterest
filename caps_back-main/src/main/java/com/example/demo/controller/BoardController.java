package com.example.demo.controller;

import com.example.demo.dto.board.BoardResponse;
import com.example.demo.dto.board.CreateBoardRequest;
import com.example.demo.dto.board.ReorderBoardsRequest;
import com.example.demo.dto.board.SearchBoardsRequest;
import com.example.demo.dto.board.UpdateBoardRequest;
import com.example.demo.service.BoardService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/boards")
public class BoardController {

    private final BoardService boardService;

    @Autowired
    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @PostMapping
    public ResponseEntity<BoardResponse> createBoard(@Valid @RequestBody CreateBoardRequest request) {
        BoardResponse response = boardService.createBoard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<BoardResponse> getBoardById(@PathVariable Long boardId) {
        return ResponseEntity.ok(boardService.getBoardById(boardId));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<BoardResponse>> getBoardsForOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(boardService.getBoardsForOwner(ownerId));
    }

    @PostMapping("/search")
    public ResponseEntity<List<BoardResponse>> searchBoards(@Valid @RequestBody SearchBoardsRequest request) {
        return ResponseEntity.ok(boardService.searchBoards(request));
    }

    @PutMapping("/{boardId}")
    public ResponseEntity<BoardResponse> updateBoard(
            @PathVariable Long boardId, @Valid @RequestBody UpdateBoardRequest request) {
        return ResponseEntity.ok(boardService.updateBoard(boardId, request));
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long boardId) {
        boardService.deleteBoard(boardId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/owner/{ownerId}/reorder")
    public ResponseEntity<List<BoardResponse>> reorderBoards(
            @PathVariable Long ownerId, @Valid @RequestBody ReorderBoardsRequest request) {
        return ResponseEntity.ok(boardService.reorderBoards(ownerId, request));
    }
}

