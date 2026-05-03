package com.example.demo.controller;

import com.example.demo.dto.follow.FollowRequest;
import com.example.demo.dto.follow.FollowerResponse;
import com.example.demo.service.FollowerService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/followers")
public class FollowerController {

    private final FollowerService followerService;

    @Autowired
    public FollowerController(FollowerService followerService) {
        this.followerService = followerService;
    }

    @PostMapping
    public ResponseEntity<Void> follow(@Valid @RequestBody FollowRequest request) {
        followerService.follow(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> unfollow(@Valid @RequestBody FollowRequest request) {
        followerService.unfollow(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<FollowerResponse>> getFollowers(@PathVariable Long userId) {
        return ResponseEntity.ok(followerService.getFollowers(userId));
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<List<FollowerResponse>> getFollowing(@PathVariable Long userId) {
        return ResponseEntity.ok(followerService.getFollowing(userId));
    }
}

