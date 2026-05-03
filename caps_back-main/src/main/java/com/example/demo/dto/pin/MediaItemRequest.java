package com.example.demo.dto.pin;

import com.example.demo.entity.enums.MediaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MediaItemRequest {

    @NotBlank(message = "Media URL is required")
    @Size(max = 500, message = "Media URL cannot exceed 500 characters")
    private String url;

    @NotNull(message = "Media type is required")
    private MediaType mediaType = MediaType.IMAGE;

    private Integer position = 0;
}

