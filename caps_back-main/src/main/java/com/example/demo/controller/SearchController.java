package com.example.demo.controller;

import com.example.demo.dto.search.SearchResponse;
import com.example.demo.dto.search.SearchSort;
import com.example.demo.dto.search.SearchType;
import com.example.demo.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<SearchResponse> search(
            @RequestParam("query") String query,
            @RequestParam(value = "type", required = false) String typeParam,
            @RequestParam(value = "sort", required = false) String sortParam) {

        SearchType type = SearchType.from(typeParam);
        SearchSort sort = SearchSort.from(sortParam);

        return ResponseEntity.ok(searchService.search(query, type, sort));
    }
}

