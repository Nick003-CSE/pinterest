package com.example.demo.repository;

import com.example.demo.entity.PinLike;
import com.example.demo.entity.PinLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PinLikeRepository extends JpaRepository<PinLike, PinLikeId> {

    boolean existsByPinIdAndUserId(Long pinId, Long userId);

    void deleteByPinIdAndUserId(Long pinId, Long userId);

    @Query("SELECT COUNT(pl) FROM PinLike pl WHERE pl.pin.id = :pinId")
    Long countByPinId(@Param("pinId") Long pinId);
}

