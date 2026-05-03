package com.example.demo.repository;

import com.example.demo.entity.Board;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardRepository extends JpaRepository<Board, Long> {
    List<Board> findByOwnerId(Long ownerId);

    boolean existsByOwnerIdAndNameIgnoreCase(Long ownerId, String name);

    @Query("SELECT b FROM Board b WHERE b.owner.id = :ownerId AND "
            + "(:keyword IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Board> searchBoards(@Param("ownerId") Long ownerId, @Param("keyword") String keyword);

    @Query("SELECT DISTINCT b FROM Board b JOIN FETCH b.owner o WHERE b.visibility = 'PUBLIC' AND "
            + "(:keyword IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(o.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(o.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Board> searchPublicBoards(@Param("keyword") String keyword);

    @Query("SELECT DISTINCT b FROM Board b WHERE b.visibility = 'PUBLIC' AND "
            + "(LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
            + "ORDER BY CASE WHEN LOWER(b.name) LIKE LOWER(CONCAT(:keyword, '%')) THEN 0 ELSE 1 END, b.createdAt DESC")
    List<Board> searchPublicBoardsWithSuggestions(@Param("keyword") String keyword);
}

