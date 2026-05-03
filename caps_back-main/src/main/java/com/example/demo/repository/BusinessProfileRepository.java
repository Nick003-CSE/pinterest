package com.example.demo.repository;

import com.example.demo.entity.BusinessProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, Long> {

    Optional<BusinessProfile> findByUsername(String username);

    @Query("SELECT b FROM BusinessProfile b WHERE b.name LIKE %:searchTerm% OR b.description LIKE %:searchTerm% OR b.category LIKE %:searchTerm%")
    List<BusinessProfile> searchBusinessProfiles(@Param("searchTerm") String searchTerm);

    List<BusinessProfile> findByCategory(String category);

    List<BusinessProfile> findByVerifiedTrue();
}

