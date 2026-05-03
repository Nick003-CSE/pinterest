package com.example.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.dto.follow.FollowerResponse;
import com.example.demo.service.FollowerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class FollowerControllerTest {

    @Mock
    private FollowerService followerService;

    @InjectMocks
    private FollowerController followerController;


    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(followerController).build();
    }

    @Test
    void follow_shouldReturnOkAndCallService() throws Exception {
        String body = """
                {
                  "followerId": 1,
                  "followingId": 2
                }
                """;

        mockMvc()
                .perform(post("/followers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        verify(followerService).follow(any());
    }

    @Test
    void follow_shouldHandleInvalidJson() throws Exception {
        String invalidBody = "{ invalid json }";

        mockMvc()
                .perform(post("/followers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void follow_shouldHandleMissingFields() throws Exception {
        String body = """
                {
                  "followerId": 1
                }
                """;

        mockMvc()
                .perform(post("/followers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unfollow_shouldReturnNoContentAndCallService() throws Exception {
        String body = """
                {
                  "followerId": 1,
                  "followingId": 2
                }
                """;

        mockMvc()
                .perform(delete("/followers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());

        verify(followerService).unfollow(any());
    }

    @Test
    void unfollow_shouldHandleInvalidJson() throws Exception {
        String invalidBody = "{ invalid }";

        mockMvc()
                .perform(delete("/followers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getFollowers_shouldReturnListFromService() throws Exception {
        FollowerResponse resp = FollowerResponse.builder()
                .userId(1L)
                .username("testuser")
                .fullName("Test User")
                .email("test@example.com")
                .phoneNumber("1234567890")
                .build();

        when(followerService.getFollowers(2L)).thenReturn(Collections.singletonList(resp));

        mockMvc()
                .perform(get("/followers/2/followers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userId").value(1L))
                .andExpect(jsonPath("$[0].username").value("testuser"))
                .andExpect(jsonPath("$[0].fullName").value("Test User"))
                .andExpect(jsonPath("$[0].email").value("test@example.com"))
                .andExpect(jsonPath("$[0].phoneNumber").value("1234567890"));

        verify(followerService).getFollowers(2L);
    }

    @Test
    void getFollowers_shouldReturnEmptyList_whenNoFollowers() throws Exception {
        when(followerService.getFollowers(2L)).thenReturn(Collections.emptyList());

        mockMvc()
                .perform(get("/followers/2/followers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(followerService).getFollowers(2L);
    }

    @Test
    void getFollowers_shouldReturnMultipleFollowers() throws Exception {
        FollowerResponse resp1 = FollowerResponse.builder()
                .userId(1L)
                .username("user1")
                .fullName("User One")
                .build();
        FollowerResponse resp2 = FollowerResponse.builder()
                .userId(3L)
                .username("user3")
                .fullName("User Three")
                .build();

        when(followerService.getFollowers(2L)).thenReturn(Arrays.asList(resp1, resp2));

        mockMvc()
                .perform(get("/followers/2/followers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userId").value(1L))
                .andExpect(jsonPath("$[1].userId").value(3L));

        verify(followerService).getFollowers(2L);
    }

    @Test
    void getFollowing_shouldReturnListFromService() throws Exception {
        FollowerResponse resp = FollowerResponse.builder()
                .userId(3L)
                .username("followinguser")
                .fullName("Following User")
                .email("following@example.com")
                .build();

        when(followerService.getFollowing(1L)).thenReturn(Collections.singletonList(resp));

        mockMvc()
                .perform(get("/followers/1/following"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userId").value(3L))
                .andExpect(jsonPath("$[0].username").value("followinguser"));

        verify(followerService).getFollowing(1L);
    }

    @Test
    void getFollowing_shouldReturnEmptyList_whenNotFollowingAnyone() throws Exception {
        when(followerService.getFollowing(1L)).thenReturn(Collections.emptyList());

        mockMvc()
                .perform(get("/followers/1/following"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(followerService).getFollowing(1L);
    }

    @Test
    void getFollowing_shouldReturnMultipleFollowing() throws Exception {
        FollowerResponse resp1 = FollowerResponse.builder()
                .userId(2L)
                .username("user2")
                .build();
        FollowerResponse resp2 = FollowerResponse.builder()
                .userId(4L)
                .username("user4")
                .build();

        when(followerService.getFollowing(1L)).thenReturn(Arrays.asList(resp1, resp2));

        mockMvc()
                .perform(get("/followers/1/following"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userId").value(2L))
                .andExpect(jsonPath("$[1].userId").value(4L));

        verify(followerService).getFollowing(1L);
    }
}
