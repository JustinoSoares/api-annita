package com.example.annita.controller;

import com.example.annita.dto.EventRequest;
import com.example.annita.dto.EventResponse;
import com.example.annita.dto.PageResponse;
import com.example.annita.dto.ReportRequest;
import com.example.annita.model.EventModality;
import com.example.annita.model.EventStatus;
import com.example.annita.model.EventType;
import com.example.annita.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @Operation(summary = "Create a new event", description = "Accessible by CONTRIBUTOR, MODERATOR, and ADMIN roles.")
    public ResponseEntity<EventResponse> create(@Valid @RequestBody EventRequest request, Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        EventResponse response = eventService.create(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List approved events publicly with pagination and filters")
    public ResponseEntity<PageResponse<EventResponse>> getApproved(
            @Parameter(description = "Search term to match against title or description") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by category ID") @RequestParam(required = false) UUID categoryId,
            @Parameter(description = "Filter by modality") @RequestParam(required = false) EventModality modality,
            @Parameter(description = "Filter by type") @RequestParam(required = false) EventType type,
            @Parameter(description = "Page number (1-indexed)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page") @RequestParam(name = "per_page", defaultValue = "10") int perPage) {
        PageResponse<EventResponse> response = eventService.getApproved(search, categoryId, modality, type, page, perPage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MODERATOR')")
    @Operation(summary = "List all events with filters for administration", description = "Accessible only by ADMIN or MODERATOR roles.")
    public ResponseEntity<PageResponse<EventResponse>> getAll(
            @Parameter(description = "Search term to match against title or description") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by category ID") @RequestParam(required = false) UUID categoryId,
            @Parameter(description = "Filter by modality") @RequestParam(required = false) EventModality modality,
            @Parameter(description = "Filter by type") @RequestParam(required = false) EventType type,
            @Parameter(description = "Filter by status") @RequestParam(required = false) EventStatus status,
            @Parameter(description = "Page number (1-indexed)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page") @RequestParam(name = "per_page", defaultValue = "10") int perPage) {
        PageResponse<EventResponse> response = eventService.getAll(search, categoryId, modality, type, status, page, perPage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyAuthority('SCOPE_CONTRIBUTOR', 'SCOPE_MODERATOR', 'SCOPE_ADMIN')")
    @Operation(summary = "List events created by the authenticated user")
    public ResponseEntity<PageResponse<EventResponse>> getMyEvents(
            @Parameter(description = "Page number (1-indexed)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page") @RequestParam(name = "per_page", defaultValue = "10") int perPage,
            Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        PageResponse<EventResponse> response = eventService.getByUser(userId, page, perPage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an approved event by ID (public)")
    public ResponseEntity<EventResponse> getById(@PathVariable UUID id) {
        EventResponse response = eventService.getApprovedById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/details")
    @PreAuthorize("hasAnyAuthority('SCOPE_CONTRIBUTOR', 'SCOPE_MODERATOR', 'SCOPE_ADMIN')")
    @Operation(summary = "Get full event details by ID (authenticated users)")
    public ResponseEntity<EventResponse> getDetailsById(@PathVariable UUID id) {
        EventResponse response = eventService.getById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_CONTRIBUTOR', 'SCOPE_MODERATOR', 'SCOPE_ADMIN')")
    @Operation(summary = "Update an event", description = "Accessible by the owner, MODERATOR, or ADMIN.")
    public ResponseEntity<EventResponse> update(@PathVariable UUID id, @Valid @RequestBody EventRequest request, Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        EventResponse response = eventService.update(id, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_CONTRIBUTOR', 'SCOPE_MODERATOR', 'SCOPE_ADMIN')")
    @Operation(summary = "Delete an event", description = "Accessible by the owner, MODERATOR, or ADMIN.")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        eventService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MODERATOR')")
    @Operation(summary = "Approve a pending event", description = "Accessible only by ADMIN or MODERATOR roles.")
    public ResponseEntity<EventResponse> approve(@PathVariable UUID id) {
        EventResponse response = eventService.approve(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MODERATOR')")
    @Operation(summary = "Reject a pending event", description = "Accessible only by ADMIN or MODERATOR roles.")
    public ResponseEntity<EventResponse> reject(@PathVariable UUID id) {
        EventResponse response = eventService.reject(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/report")
    @PreAuthorize("hasAnyAuthority('SCOPE_CONTRIBUTOR', 'SCOPE_MODERATOR', 'SCOPE_ADMIN')")
    @Operation(summary = "Report an approved event", description = "Accessible by any authenticated user.")
    public ResponseEntity<EventResponse> report(@PathVariable UUID id, @Valid @RequestBody ReportRequest request, Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        EventResponse response = eventService.report(id, request, userId);
        return ResponseEntity.ok(response);
    }
}
