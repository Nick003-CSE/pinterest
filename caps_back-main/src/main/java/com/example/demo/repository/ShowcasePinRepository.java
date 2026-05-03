package com.example.demo.repository;

import com.example.demo.entity.ShowcasePin;
import com.example.demo.entity.ShowcasePinId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShowcasePinRepository extends JpaRepository<ShowcasePin, ShowcasePinId> {

    @Query("SELECT sp FROM ShowcasePin sp JOIN FETCH sp.pin WHERE sp.showcase.id = :showcaseId ORDER BY sp.position ASC")
    List<ShowcasePin> findByShowcaseId(@Param("showcaseId") Long showcaseId);
}

