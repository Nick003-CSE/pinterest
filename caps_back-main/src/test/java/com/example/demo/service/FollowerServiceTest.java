package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.dto.follow.FollowRequest;
import com.example.demo.dto.follow.FollowerResponse;
import com.example.demo.entity.Follower;
import com.example.demo.entity.FollowerId;
import com.example.demo.entity.UserAccount;
import com.example.demo.exception.BusinessValidationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.FollowerRepository;
import com.example.demo.repository.UserAccountRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FollowerServiceTest {

    @Mock
    private FollowerRepository followerRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private FollowerService followerService;

    @Test
    void follow_shouldThrow_whenUserTriesToFollowSelf() {
        FollowRequest request = new FollowRequest();
        request.setFollowerId(1L);
        request.setFollowingId(1L);

        assertThatThrownBy(() -> followerService.follow(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Users cannot follow themselves");
    }

    @Test
    void follow_shouldThrow_whenFollowerNotFound() {
        FollowRequest request = new FollowRequest();
        request.setFollowerId(1L);
        request.setFollowingId(2L);

        when(userAccountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> followerService.follow(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void follow_shouldThrow_whenFollowingNotFound() {
        FollowRequest request = new FollowRequest();
        request.setFollowerId(1L);
        request.setFollowingId(2L);

        UserAccount follower = new UserAccount();
        follower.setId(1L);

        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(follower));
        when(userAccountRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> followerService.follow(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void follow_shouldReturnSilently_whenRelationAlreadyExists() {
        FollowRequest request = new FollowRequest();
        request.setFollowerId(1L);
        request.setFollowingId(2L);

        UserAccount follower = new UserAccount();
        follower.setId(1L);
        UserAccount following = new UserAccount();
        following.setId(2L);

        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(follower));
        when(userAccountRepository.findById(2L)).thenReturn(Optional.of(following));
        when(followerRepository.existsById(new FollowerId(1L, 2L))).thenReturn(true);

        followerService.follow(request);

        verify(followerRepository, never()).save(any(Follower.class));
    }

    @Test
    void follow_shouldCreateRelation_whenNotExisting() {
        FollowRequest request = new FollowRequest();
        request.setFollowerId(1L);
        request.setFollowingId(2L);

        UserAccount follower = new UserAccount();
        follower.setId(1L);
        follower.setUsername("follower");
        follower.setFullName("Follower User");
        follower.setEmail("follower@test.com");
        follower.setPhoneNumber("1234567890");

        UserAccount following = new UserAccount();
        following.setId(2L);
        following.setUsername("following");
        following.setFullName("Following User");

        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(follower));
        when(userAccountRepository.findById(2L)).thenReturn(Optional.of(following));
        when(followerRepository.existsById(new FollowerId(1L, 2L))).thenReturn(false);

        followerService.follow(request);

        ArgumentCaptor<Follower> captor = ArgumentCaptor.forClass(Follower.class);
        verify(followerRepository).save(captor.capture());
        Follower saved = captor.getValue();
        assertThat(saved.getId().getFollowerId()).isEqualTo(1L);
        assertThat(saved.getId().getFollowingId()).isEqualTo(2L);
        assertThat(saved.getFollower().getId()).isEqualTo(1L);
        assertThat(saved.getFollowing().getId()).isEqualTo(2L);
    }

    @Test
    void unfollow_shouldDoNothing_whenSelfUnfollowAttempt() {
        FollowRequest request = new FollowRequest();
        request.setFollowerId(1L);
        request.setFollowingId(1L);

        followerService.unfollow(request);

        verify(followerRepository, never()).deleteById(any(FollowerId.class));
    }

    @Test
    void unfollow_shouldDeleteRelation_whenDifferentUsers() {
        FollowRequest request = new FollowRequest();
        request.setFollowerId(1L);
        request.setFollowingId(2L);

        FollowerId expectedId = new FollowerId(1L, 2L);
        followerService.unfollow(request);

        verify(followerRepository).deleteById(expectedId);
    }

    @Test
    void getFollowers_shouldReturnEmptyList_whenNoFollowers() {
        when(followerRepository.findFollowers(1L)).thenReturn(Collections.emptyList());

        List<FollowerResponse> result = followerService.getFollowers(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void getFollowers_shouldReturnMappedResponses() {
        UserAccount follower1 = new UserAccount();
        follower1.setId(10L);
        follower1.setUsername("follower1");
        follower1.setFullName("Follower One");
        follower1.setEmail("f1@test.com");
        follower1.setPhoneNumber("1111111111");

        UserAccount follower2 = new UserAccount();
        follower2.setId(20L);
        follower2.setUsername("follower2");
        follower2.setFullName("Follower Two");
        follower2.setEmail("f2@test.com");
        follower2.setPhoneNumber("2222222222");

        Follower relation1 = new Follower();
        relation1.setFollower(follower1);
        Follower relation2 = new Follower();
        relation2.setFollower(follower2);

        when(followerRepository.findFollowers(1L)).thenReturn(Arrays.asList(relation1, relation2));

        List<FollowerResponse> result = followerService.getFollowers(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUserId()).isEqualTo(10L);
        assertThat(result.get(0).getUsername()).isEqualTo("follower1");
        assertThat(result.get(0).getFullName()).isEqualTo("Follower One");
        assertThat(result.get(0).getEmail()).isEqualTo("f1@test.com");
        assertThat(result.get(1).getUserId()).isEqualTo(20L);
    }

    @Test
    void getFollowing_shouldReturnEmptyList_whenNotFollowingAnyone() {
        when(followerRepository.findFollowing(1L)).thenReturn(Collections.emptyList());

        List<FollowerResponse> result = followerService.getFollowing(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void getFollowing_shouldReturnMappedResponses() {
        UserAccount following1 = new UserAccount();
        following1.setId(30L);
        following1.setUsername("following1");
        following1.setFullName("Following One");
        following1.setEmail("fo1@test.com");
        following1.setPhoneNumber("3333333333");

        UserAccount following2 = new UserAccount();
        following2.setId(40L);
        following2.setUsername("following2");
        following2.setFullName("Following Two");
        following2.setEmail("fo2@test.com");
        following2.setPhoneNumber("4444444444");

        Follower relation1 = new Follower();
        relation1.setFollowing(following1);
        Follower relation2 = new Follower();
        relation2.setFollowing(following2);

        when(followerRepository.findFollowing(1L)).thenReturn(Arrays.asList(relation1, relation2));

        List<FollowerResponse> result = followerService.getFollowing(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUserId()).isEqualTo(30L);
        assertThat(result.get(0).getUsername()).isEqualTo("following1");
        assertThat(result.get(1).getUserId()).isEqualTo(40L);
        assertThat(result.get(1).getUsername()).isEqualTo("following2");
    }

    @Test
    void getFollowers_shouldHandleNullFieldsGracefully() {
        UserAccount follower = new UserAccount();
        follower.setId(50L);
        follower.setUsername("user");
        follower.setFullName(null);
        follower.setEmail(null);
        follower.setPhoneNumber(null);

        Follower relation = new Follower();
        relation.setFollower(follower);

        when(followerRepository.findFollowers(1L)).thenReturn(Collections.singletonList(relation));

        List<FollowerResponse> result = followerService.getFollowers(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(50L);
        assertThat(result.get(0).getUsername()).isEqualTo("user");
        assertThat(result.get(0).getFullName()).isNull();
        assertThat(result.get(0).getEmail()).isNull();
        assertThat(result.get(0).getPhoneNumber()).isNull();
    }

    @Test
    void getFollowing_shouldHandleNullFieldsGracefully() {
        UserAccount following = new UserAccount();
        following.setId(60L);
        following.setUsername("user2");
        following.setFullName(null);
        following.setEmail(null);
        following.setPhoneNumber(null);

        Follower relation = new Follower();
        relation.setFollowing(following);

        when(followerRepository.findFollowing(1L)).thenReturn(Collections.singletonList(relation));

        List<FollowerResponse> result = followerService.getFollowing(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(60L);
        assertThat(result.get(0).getUsername()).isEqualTo("user2");
    }
}
