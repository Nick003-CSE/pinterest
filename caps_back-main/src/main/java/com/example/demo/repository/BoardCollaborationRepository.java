package com.example.demo.repository;

import com.example.demo.entity.BoardCollaboration;
import com.example.demo.entity.BoardCollaborationId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardCollaborationRepository extends JpaRepository<BoardCollaboration, BoardCollaborationId> {

    @Query("SELECT bc FROM BoardCollaboration bc WHERE bc.collaborator.id = :userId")
    List<BoardCollaboration> findByCollaboratorId(@Param("userId") Long userId);

    @Query("SELECT bc FROM BoardCollaboration bc WHERE bc.board.id = :boardId")
    List<BoardCollaboration> findByBoardId(@Param("boardId") Long boardId);
}


