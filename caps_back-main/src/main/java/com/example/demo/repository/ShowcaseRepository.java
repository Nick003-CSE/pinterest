package com.example.demo.repository;

import com.example.demo.entity.Showcase;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShowcaseRepository extends JpaRepository<Showcase, Long> {

    List<Showcase> findByBusinessProfileId(Long businessProfileId);

    List<Showcase> findByTheme(String theme);

    List<Showcase> findByFeaturedTrue();

    @Query("SELECT s FROM Showcase s WHERE s.businessProfile.id = :businessId ORDER BY s.featured DESC, s.createdAt DESC")
    List<Showcase> findByBusinessProfileIdOrderByFeatured(@Param("businessId") Long businessId);
}

