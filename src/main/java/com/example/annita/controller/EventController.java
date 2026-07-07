package com.example.annita.controller;

import com.example.annita.dto.EventRequest;
import com.example.annita.dto.EventResponse;
import com.example.annita.dto.PageResponse;
import com.example.annita.dto.ReportRequest;
import com.example.annita.dto.VoteRequest;
import com.example.annita.model.EventModality;
import com.example.annita.model.EventStatus;
import com.example.annita.model.EventType;
import com.example.annita.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Events", description = "Endpoints for managing events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_CONTRIBUTOR', 'SCOPE_MODERATOR', 'SCOPE_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a new event", description = "Accessible by CONTRIBUTOR, MODERATOR, and ADMIN roles.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Event created successfully",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = EventResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<EventResponse> create(@Valid @RequestBody EventRequest request, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        EventResponse response = eventService.create(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List events with pagination and filters", description = "Admin/Moderator: all events. Authenticated user: own events. Anonymous: approved events only.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Events retrieved successfully",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = PageResponse.class)))
    })
    public ResponseEntity<PageResponse<EventResponse>> getEvents(
            @Parameter(description = "Search term to match against title or description") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by category ID") @RequestParam(required = false) UUID categoryId,
            @Parameter(description = "Filter by modality") @RequestParam(required = false) EventModality modality,
            @Parameter(description = "Filter by type") @RequestParam(required = false) EventType type,
            @Parameter(description = "Filter by status (admin/mod only)") @RequestParam(required = false) EventStatus status,
            @Parameter(description = "Page number (1-indexed)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page") @RequestParam(name = "per_page", defaultValue = "10") int perPage,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = jwt != null ? UUID.fromString(jwt.getClaim("userId")) : null;
        String role = jwt != null ? jwt.getClaimAsString("scope") : null;
        PageResponse<EventResponse> response = eventService.getEvents(search, categoryId, modality, type, status, userId, role, page, perPage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MODERATOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List all events with filters for administration", description = "Accessible only by ADMIN or MODERATOR roles.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Events retrieved successfully",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = PageResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden — requires ADMIN or MODERATOR role")
    })
    public ResponseEntity<PageResponse<EventResponse>> getAll(
            @Parameter(description = "Search term to match against title or description") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by category ID") @RequestParam(required = false) UUID categoryId,
            @Parameter(description = "Filter by modality") @RequestParam(required = false) EventModality modality,
            @Parameter(description = "Filter by type") @RequestParam(required = false) EventType type,
            @Parameter(description = "Filter by status") @RequestParam(required = false) EventStatus status,
            @Parameter(description = "Page number (1-indexed)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page") @RequestParam(name = "per_page", defaultValue = "10") int perPage,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        PageResponse<EventResponse> response = eventService.getAll(search, categoryId, modality, type, status, page, perPage, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyAuthority('SCOPE_CONTRIBUTOR', 'SCOPE_MODERATOR', 'SCOPE_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List events created by the authenticated user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User's events retrieved successfully",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = PageResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<PageResponse<EventResponse>> getMyEvents(
            @Parameter(description = "Page number (1-indexed)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page") @RequestParam(name = "per_page", defaultValue = "10") int perPage,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        PageResponse<EventResponse> response = eventService.getByUser(userId, page, perPage, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Get an approved event by ID (public)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Event found",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = EventResponse.class))),
        @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<EventResponse> getById(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = jwt != null ? UUID.fromString(jwt.getClaim("userId")) : null;
        EventResponse response = userId != null ? eventService.getApprovedById(id, userId) : eventService.getApprovedById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/details")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Get full event details by ID (authenticated users)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Event details retrieved",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = EventResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<EventResponse> getDetailsById(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        EventResponse response = eventService.getById(id, userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_CONTRIBUTOR', 'SCOPE_MODERATOR', 'SCOPE_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update an event", description = "Accessible by the owner, MODERATOR, or ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Event updated successfully",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = EventResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden — not the owner, MODERATOR, or ADMIN"),
        @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<EventResponse> update(@PathVariable UUID id, @Valid @RequestBody EventRequest request, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        EventResponse response = eventService.update(id, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_CONTRIBUTOR', 'SCOPE_MODERATOR', 'SCOPE_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete an event", description = "Accessible by the owner, MODERATOR, or ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Event deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden — not the owner, MODERATOR, or ADMIN"),
        @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        eventService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MODERATOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Approve a pending event", description = "Accessible only by ADMIN or MODERATOR roles.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Event approved successfully",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = EventResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden — requires ADMIN or MODERATOR role"),
        @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<EventResponse> approve(@PathVariable UUID id) {
        EventResponse response = eventService.approve(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MODERATOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Reject a pending event", description = "Accessible only by ADMIN or MODERATOR roles.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Event rejected successfully",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = EventResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden — requires ADMIN or MODERATOR role"),
        @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<EventResponse> reject(@PathVariable UUID id) {
        EventResponse response = eventService.reject(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/vote")
    @PreAuthorize("hasAnyAuthority('SCOPE_CONTRIBUTOR', 'SCOPE_MODERATOR', 'SCOPE_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Upvote or downvote an event", description = "Toggle vote: same type removes the vote, different type switches.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Vote registered",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = EventResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error or event not approved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<EventResponse> vote(@PathVariable UUID id, @Valid @RequestBody VoteRequest request, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        EventResponse response = eventService.vote(id, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/vote")
    @PreAuthorize("hasAnyAuthority('SCOPE_CONTRIBUTOR', 'SCOPE_MODERATOR', 'SCOPE_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Remove vote from an event", description = "Unvote/remove the authenticated user's vote.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Vote removed",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = EventResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<EventResponse> unvote(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        EventResponse response = eventService.unvote(id, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/report")
    @PreAuthorize("hasAnyAuthority('SCOPE_CONTRIBUTOR', 'SCOPE_MODERATOR', 'SCOPE_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Report an approved event", description = "Accessible by any authenticated user.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Event reported successfully",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = EventResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid reason or already reported"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<EventResponse> report(@PathVariable UUID id, @Valid @RequestBody ReportRequest request, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        EventResponse response = eventService.report(id, request, userId);
        return ResponseEntity.ok(response);
    }
}
