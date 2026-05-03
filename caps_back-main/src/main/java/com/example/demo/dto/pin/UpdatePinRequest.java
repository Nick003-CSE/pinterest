package com.example.demo.dto.pin;

import com.example.demo.entity.enums.MediaType;
import com.example.demo.entity.enums.PinStatus;
import com.example.demo.entity.enums.PinVisibility;
import com.example.demo.entity.enums.BoardVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePinRequest {

    @NotNull
    private Long ownerId;

    private Long boardId;

    private String newBoardName;

    private String newBoardDescription;

    private BoardVisibility newBoardVisibility = BoardVisibility.PUBLIC;

    private Boolean detachFromBoard = Boolean.FALSE;

    @NotBlank
    private String title;

    private String description;

    private MediaType mediaType = MediaType.IMAGE;

    private String mediaUrl;

    private String sourceUrl;

    private String attribution;

    private Set<String> keywords;

    private PinVisibility visibility = PinVisibility.PUBLIC;

    private PinStatus status = PinStatus.DRAFT;

    private List<MediaItemRequest> mediaItems;
}

