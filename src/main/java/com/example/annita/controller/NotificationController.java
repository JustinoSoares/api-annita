package com.example.annita.controller;

import com.example.annita.dto.NotificationResponse;
import com.example.annita.dto.PageResponse;
import com.example.annita.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Notifications", description = "Endpoints for managing notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_CONTRIBUTOR', 'SCOPE_MODERATOR', 'SCOPE_ADMIN')")
    @Operation(summary = "List notifications for the authenticated user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = PageResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<PageResponse<NotificationResponse>> getNotifications(
            @Parameter(description = "Page number (1-indexed)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page") @RequestParam(name = "per_page", defaultValue = "10") int perPage,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        PageResponse<NotificationResponse> response = notificationService.getNotifications(userId, page, perPage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyAuthority('SCOPE_CONTRIBUTOR', 'SCOPE_MODERATOR', 'SCOPE_ADMIN')")
    @Operation(summary = "Get unread notification count for the authenticated user")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyAuthority('SCOPE_CONTRIBUTOR', 'SCOPE_MODERATOR', 'SCOPE_ADMIN')")
    @Operation(summary = "Mark a notification as read")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Notification marked as read"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden — not your notification"),
        @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    @PreAuthorize("hasAnyAuthority('SCOPE_CONTRIBUTOR', 'SCOPE_MODERATOR', 'SCOPE_ADMIN')")
    @Operation(summary = "Mark all notifications as read")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "All notifications marked as read"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
}
