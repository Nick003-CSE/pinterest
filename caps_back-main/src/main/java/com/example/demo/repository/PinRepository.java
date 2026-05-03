package com.example.demo.repository;

import com.example.demo.entity.Pin;
import com.example.demo.entity.enums.PinStatus;
import com.example.demo.entity.enums.PinVisibility;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PinRepository extends JpaRepository<Pin, Long> {
    List<Pin> findByOwnerId(Long ownerId);

    List<Pin> findByBoardId(Long boardId);

    long countByBoardId(Long boardId);

    @Query("SELECT p FROM Pin p WHERE p.owner.id = :ownerId AND "
            + "(:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND "
            + "(:status IS NULL OR p.status = :status) AND "
            + "(:visibility IS NULL OR p.visibility = :visibility)")
    List<Pin> searchPins(
            @Param("ownerId") Long ownerId,
            @Param("keyword") String keyword,
            @Param("status") PinStatus status,
            @Param("visibility") PinVisibility visibility);

    @Query("SELECT p FROM Pin p WHERE p.visibility = 'PUBLIC' AND p.status = 'PUBLISHED' AND "
            + "(:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "EXISTS (SELECT k FROM p.keywords k WHERE LOWER(k) LIKE LOWER(CONCAT('%', :keyword, '%'))))")
    List<Pin> searchPublicPins(@Param("keyword") String keyword);

    @Query("SELECT DISTINCT p FROM Pin p WHERE p.visibility = 'PUBLIC' AND p.status = 'PUBLISHED' AND "
            + "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
            + "ORDER BY CASE WHEN LOWER(p.title) LIKE LOWER(CONCAT(:keyword, '%')) THEN 0 ELSE 1 END, p.createdAt DESC")
    List<Pin> searchPublicPinsWithSuggestions(@Param("keyword") String keyword);

    @Query(
            """
            SELECT DISTINCT p FROM Pin p
            JOIN FETCH p.owner o
            LEFT JOIN FETCH p.board b
            LEFT JOIN p.keywords kw
            WHERE p.status = com.example.demo.entity.enums.PinStatus.PUBLISHED
              AND p.visibility = com.example.demo.entity.enums.PinVisibility.PUBLIC
              AND (
                    LOWER(p.title) LIKE LOWER(CONCAT('%', :term, '%'))
                 OR LOWER(p.description) LIKE LOWER(CONCAT('%', :term, '%'))
                 OR LOWER(o.username) LIKE LOWER(CONCAT('%', :term, '%'))
                 OR LOWER(o.fullName) LIKE LOWER(CONCAT('%', :term, '%'))
                 OR (b IS NOT NULL AND LOWER(b.name) LIKE LOWER(CONCAT('%', :term, '%')))
                 OR LOWER(kw) LIKE LOWER(CONCAT('%', :term, '%'))
              )
            """)
    List<Pin> searchPublishedPins(@Param("term") String term);

    @Query(
            """
            SELECT p FROM Pin p
            JOIN FETCH p.board b
            WHERE b.id IN :boardIds
              AND p.status = :status
            ORDER BY b.id ASC, p.createdAt DESC
            """)
    List<Pin> findLatestPinsForBoards(
            @Param("boardIds") Collection<Long> boardIds, @Param("status") PinStatus status);

    @Query(
            """
            SELECT DISTINCT p FROM Pin p
            JOIN FETCH p.owner o
            LEFT JOIN FETCH p.board b
            WHERE p.status = com.example.demo.entity.enums.PinStatus.PUBLISHED
              AND p.visibility = com.example.demo.entity.enums.PinVisibility.PUBLIC
            ORDER BY p.createdAt DESC
            """)
    List<Pin> findPublishedPublicPins();
}

