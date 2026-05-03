package com.example.demo.dto;

import java.time.Instant;
import java.util.List;
import lombok.Getter;

@Getter
public class ErrorResponse {

    private final Instant timestamp = Instant.now();
    private final String message;
    private final List<String> details;

    public ErrorResponse(String message, List<String> details) {
        this.message = message;
        this.details = details;
    }
}

