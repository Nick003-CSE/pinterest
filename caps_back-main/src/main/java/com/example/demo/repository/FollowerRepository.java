package com.example.demo.repository;

import com.example.demo.entity.Follower;
import com.example.demo.entity.FollowerId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowerRepository extends JpaRepository<Follower, FollowerId> {

    @Query("SELECT f FROM Follower f WHERE f.follower.id = :userId")
    List<Follower> findFollowing(@Param("userId") Long userId);

    @Query("SELECT f FROM Follower f WHERE f.following.id = :userId")
    List<Follower> findFollowers(@Param("userId") Long userId);

    boolean existsById(FollowerId id);

    void deleteById(FollowerId id);
}

