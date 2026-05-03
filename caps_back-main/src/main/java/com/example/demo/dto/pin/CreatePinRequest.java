package com.example.demo.dto.pin;

import com.example.demo.entity.enums.BoardVisibility;
import com.example.demo.entity.enums.MediaType;
import com.example.demo.entity.enums.PinStatus;
import com.example.demo.entity.enums.PinVisibility;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePinRequest {

    @NotNull(message = "Owner id is required")
    private Long ownerId;

    private Long boardId;

    @Size(max = 100, message = "Board name must be 100 characters or fewer")
    private String newBoardName;

    @Size(max = 500, message = "Board description must be 500 characters or fewer")
    private String newBoardDescription;

    private BoardVisibility newBoardVisibility = BoardVisibility.PUBLIC;

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must be 150 characters or fewer")
    private String title;

    private String description;

    private MediaType mediaType = MediaType.IMAGE;

    // Allow large base64 data URLs from frontend uploads. Validation is handled at the API layer.
    private String mediaUrl;

    @Size(max = 500, message = "Source URL cannot exceed 500 characters")
    private String sourceUrl;

    @Size(max = 255, message = "Attribution must be 255 characters or fewer")
    private String attribution;

    private Set<
                    @NotBlank(message = "Keyword cannot be blank")
                    @Size(max = 50, message = "Keyword cannot exceed 50 characters") String>
            keywords;

    private PinVisibility visibility = PinVisibility.PUBLIC;

    private PinStatus status = PinStatus.DRAFT;

    @Valid
    private List<MediaItemRequest> mediaItems;
}

