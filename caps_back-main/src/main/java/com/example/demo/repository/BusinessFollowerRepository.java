package com.example.demo.repository;

import com.example.demo.entity.BusinessFollower;
import com.example.demo.entity.BusinessFollowerId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BusinessFollowerRepository extends JpaRepository<BusinessFollower, BusinessFollowerId> {

    @Query("SELECT bf FROM BusinessFollower bf WHERE bf.follower.id = :userId")
    List<BusinessFollower> findByFollowerId(@Param("userId") Long userId);

    @Query("SELECT bf FROM BusinessFollower bf WHERE bf.businessProfile.id = :businessId")
    List<BusinessFollower> findByBusinessProfileId(@Param("businessId") Long businessId);

    boolean existsById(BusinessFollowerId id);
}

