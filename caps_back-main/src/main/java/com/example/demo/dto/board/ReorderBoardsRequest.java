package com.example.demo.dto.board;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReorderBoardsRequest {
    @NotEmpty(message = "Board IDs list cannot be empty")
    private List<Long> boardIds;
}

