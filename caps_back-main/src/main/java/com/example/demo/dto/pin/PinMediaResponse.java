package com.example.demo.dto.pin;

import com.example.demo.entity.enums.MediaType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PinMediaResponse {
    private Long id;
    private String url;
    private MediaType mediaType;
    private Integer position;
}

