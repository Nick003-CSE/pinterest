package com.example.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.dto.search.SearchResponse;
import com.example.demo.dto.search.SearchSort;
import com.example.demo.dto.search.SearchType;
import com.example.demo.service.SearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    @Mock
    private SearchService searchService;

    @InjectMocks
    private SearchController searchController;

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(searchController).build();
    }

    @Test
    void search_withQueryOnly_shouldReturnSearchResults() throws Exception {
        SearchResponse response = SearchResponse.builder()
                .build();

        when(searchService.search(eq("test"), any(SearchType.class), any(SearchSort.class)))
                .thenReturn(response);

        mockMvc()
                .perform(get("/search").param("query", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());

        verify(searchService).search(eq("test"), any(SearchType.class), any(SearchSort.class));
    }

    @Test
    void search_withType_shouldReturnFilteredResults() throws Exception {
        SearchResponse response = SearchResponse.builder()
                .build();

        when(searchService.search(eq("test"), eq(SearchType.PINS), any(SearchSort.class)))
                .thenReturn(response);

        mockMvc()
                .perform(get("/search")
                        .param("query", "test")
                        .param("type", "pins"))
                .andExpect(status().isOk());

        verify(searchService).search(eq("test"), eq(SearchType.PINS), any(SearchSort.class));
    }

    @Test
    void search_withSort_shouldReturnSortedResults() throws Exception {
        SearchResponse response = SearchResponse.builder()
                .build();

        when(searchService.search(eq("test"), any(SearchType.class), eq(SearchSort.RECENT)))
                .thenReturn(response);

        mockMvc()
                .perform(get("/search")
                        .param("query", "test")
                        .param("sort", "recent"))
                .andExpect(status().isOk());

        verify(searchService).search(eq("test"), any(SearchType.class), eq(SearchSort.RECENT));
    }

    @Test
    void search_withAllParameters_shouldReturnFilteredAndSortedResults() throws Exception {
        SearchResponse response = SearchResponse.builder()
                .build();

        when(searchService.search(eq("test"), eq(SearchType.BOARDS), eq(SearchSort.RELEVANCE)))
                .thenReturn(response);

        mockMvc()
                .perform(get("/search")
                        .param("query", "test")
                        .param("type", "boards")
                        .param("sort", "relevance"))
                .andExpect(status().isOk());

        verify(searchService).search(eq("test"), eq(SearchType.BOARDS), eq(SearchSort.RELEVANCE));
    }
}

