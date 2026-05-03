package com.example.demo.service;

import com.example.demo.dto.follow.FollowRequest;
import com.example.demo.dto.follow.FollowerResponse;
import com.example.demo.entity.Follower;
import com.example.demo.entity.FollowerId;
import com.example.demo.entity.UserAccount;
import com.example.demo.exception.BusinessValidationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.FollowerRepository;
import com.example.demo.repository.UserAccountRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FollowerService {

    private final FollowerRepository followerRepository;
    private final UserAccountRepository userAccountRepository;

    @Autowired
    public FollowerService(
            FollowerRepository followerRepository, UserAccountRepository userAccountRepository) {
        this.followerRepository = followerRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional
    public void follow(FollowRequest request) {
        if (request.getFollowerId().equals(request.getFollowingId())) {
            throw new BusinessValidationException("Users cannot follow themselves");
        }

        UserAccount follower = getUser(request.getFollowerId());
        UserAccount following = getUser(request.getFollowingId());

        FollowerId id = new FollowerId(follower.getId(), following.getId());
        if (followerRepository.existsById(id)) {
            return;
        }

        Follower relation = new Follower();
        relation.setId(id);
        relation.setFollower(follower);
        relation.setFollowing(following);

        followerRepository.save(relation);
    }

    @Transactional
    public void unfollow(FollowRequest request) {
        if (request.getFollowerId().equals(request.getFollowingId())) {
            return;
        }
        FollowerId id = new FollowerId(request.getFollowerId(), request.getFollowingId());
        followerRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<FollowerResponse> getFollowers(Long userId) {
        return followerRepository.findFollowers(userId).stream()
                .map(f -> mapUser(f.getFollower()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FollowerResponse> getFollowing(Long userId) {
        return followerRepository.findFollowing(userId).stream()
                .map(f -> mapUser(f.getFollowing()))
                .collect(Collectors.toList());
    }

    private UserAccount getUser(Long userId) {
        return userAccountRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private FollowerResponse mapUser(UserAccount user) {
        return FollowerResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}

