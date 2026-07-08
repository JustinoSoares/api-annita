package com.example.annita.controller;

import com.example.annita.dto.PageResponse;
import com.example.annita.dto.ReportResponse;
import com.example.annita.service.ReportService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reports", description = "Endpoints for managing reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyAuthority('SCOPE_CONTRIBUTOR', 'SCOPE_MODERATOR', 'SCOPE_ADMIN')")
    @Operation(summary = "List reports made by the authenticated user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reports retrieved successfully",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = PageResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<PageResponse<ReportResponse>> getMyReports(
            @Parameter(description = "Page number (1-indexed)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page") @RequestParam(name = "per_page", defaultValue = "10") int perPage,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        PageResponse<ReportResponse> response = reportService.getReportsByUser(userId, page, perPage);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/my")
    @PreAuthorize("hasAnyAuthority('SCOPE_CONTRIBUTOR', 'SCOPE_MODERATOR', 'SCOPE_ADMIN', 'SCOPE_COMPANY')")
    @Operation(summary = "Remove your own report", description = "Allows the owner of a report to remove it. If the event had 3+ reports and now has fewer than 3, it returns to approved.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Report removed successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden — not the owner of the report"),
        @ApiResponse(responseCode = "404", description = "Report not found")
    })
    public ResponseEntity<Void> removeMyReport(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        reportService.removeOwnReport(id, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MODERATOR')")
    @Operation(summary = "Remove a report", description = "Accessible only by ADMIN or MODERATOR. If the event had 3+ reports and now has fewer than 3, it returns to approved.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Report removed successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden — requires ADMIN or MODERATOR role"),
        @ApiResponse(responseCode = "404", description = "Report not found")
    })
    public ResponseEntity<Void> removeReport(@PathVariable UUID id) {
        reportService.removeReport(id);
        return ResponseEntity.noContent().build();
    }
}
