package com.example.demo.controller;

import com.example.demo.dto.GlobalSearchRequest;
import com.example.demo.dto.GlobalSearchResponse;
import com.example.demo.dto.SearchSuggestionResponse;
import com.example.demo.dto.pin.CreatePinRequest;
import com.example.demo.dto.pin.PinEngagementResponse;
import com.example.demo.dto.pin.PinResponse;
import com.example.demo.dto.pin.SearchPinsRequest;
import com.example.demo.dto.pin.UpdatePinRequest;
import com.example.demo.service.PinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pins")
@Tag(name = "Pins", description = "API endpoints for managing pins (images, videos, and content)")
public class PinController {

    private final PinService pinService;

    @Autowired
    public PinController(PinService pinService) {
        this.pinService = pinService;
    }

    @Operation(
            summary = "Create a new pin",
            description = "Creates a new pin with the provided details. The pin can be an image or video."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Pin created successfully",
                    content = @Content(schema = @Schema(implementation = PinResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<PinResponse> createPin(@Valid @RequestBody CreatePinRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pinService.createPin(request));
    }

    @PutMapping("/{pinId}")
    public ResponseEntity<PinResponse> updatePin(
            @PathVariable Long pinId, @Valid @RequestBody UpdatePinRequest request) {
        return ResponseEntity.ok(pinService.updatePin(pinId, request));
    }

    @Operation(
            summary = "Get pin by ID",
            description = "Retrieves a pin by its ID. Optionally includes user-specific data like like status if userId is provided."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pin found",
                    content = @Content(schema = @Schema(implementation = PinResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Pin not found")
    })
    @GetMapping("/{pinId}")
    public ResponseEntity<PinResponse> getPinById(
            @Parameter(description = "Pin ID", required = true) @PathVariable Long pinId,
            @Parameter(description = "User ID (optional, for personalized data)") @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(pinService.getPinById(pinId, userId));
    }

    @DeleteMapping("/{pinId}")
    public ResponseEntity<Void> deletePin(
            @PathVariable Long pinId, @RequestParam(required = false) Long userId) {
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        pinService.deletePin(pinId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<PinResponse>> getPinsForOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(pinService.getPinsForOwner(ownerId));
    }

    @GetMapping("/feed")
    public ResponseEntity<List<PinResponse>> getHomeFeedPins(@RequestParam(required = false) Long viewerId) {
        return ResponseEntity.ok(pinService.getHomeFeedPins(viewerId));
    }

    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<PinResponse>> getPinsByBoard(@PathVariable Long boardId) {
        return ResponseEntity.ok(pinService.getPinsByBoard(boardId));
    }

    @PostMapping("/search")
    public ResponseEntity<List<PinResponse>> searchPins(@Valid @RequestBody SearchPinsRequest request) {
        return ResponseEntity.ok(pinService.searchPins(request));
    }

    @PostMapping("/{pinId}/save")
    public ResponseEntity<PinEngagementResponse> incrementSave(@PathVariable Long pinId) {
        return ResponseEntity.ok(pinService.incrementSaveCount(pinId));
    }

    @PostMapping("/{pinId}/share")
    public ResponseEntity<PinEngagementResponse> incrementShare(@PathVariable Long pinId) {
        return ResponseEntity.ok(pinService.incrementShareCount(pinId));
    }

    @Operation(
            summary = "Like or unlike a pin",
            description = "Toggles the like status of a pin for a specific user. If the pin is already liked, it will be unliked."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Like status toggled successfully",
                    content = @Content(schema = @Schema(implementation = PinEngagementResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "User ID is required")
    })
    @PostMapping("/{pinId}/like")
    public ResponseEntity<PinEngagementResponse> toggleLike(
            @Parameter(description = "Pin ID", required = true) @PathVariable Long pinId,
            @Parameter(description = "User ID", required = true) @RequestParam(required = false) Long userId) {
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(pinService.toggleLike(pinId, userId));
    }

    @PostMapping("/global-search")
    public ResponseEntity<GlobalSearchResponse> globalSearch(@Valid @RequestBody GlobalSearchRequest request) {
        return ResponseEntity.ok(pinService.globalSearch(request));
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<SearchSuggestionResponse>> getSearchSuggestions(
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(pinService.getSearchSuggestions(keyword));
    }
}

