package com.example.demo.dto.board;

import com.example.demo.entity.enums.BoardVisibility;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBoardRequest {
    @Size(max = 100, message = "Board name must be 100 characters or fewer")
    private String name;

    @Size(max = 500, message = "Description must be 500 characters or fewer")
    private String description;

    private BoardVisibility visibility;
}

