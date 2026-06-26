package com.example.annita.controller;

import com.example.annita.dto.*;
import com.example.annita.service.NewsletterSubscriptionService;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/newsletter")
@Tag(name = "Newsletter", description = "Endpoints for managing newsletter subscriptions")
public class NewsletterController {

    private final NewsletterSubscriptionService service;

    public NewsletterController(NewsletterSubscriptionService service) {
        this.service = service;
    }

    @PostMapping("/subscribe")
    @Operation(summary = "Subscribe to the newsletter", description = "Allows any visitor to subscribe using their name and email.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Subscribed successfully",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = NewsletterSubscriptionResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "409", description = "Email already subscribed")
    })
    public ResponseEntity<NewsletterSubscriptionResponse> subscribe(@Valid @RequestBody SubscribeRequest request) {
        NewsletterSubscriptionResponse response = service.subscribe(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/unsubscribe/request")
    @Operation(summary = "Request unsubscribe code", description = "Sends a 6-digit verification code to the subscriber's email.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Verification code sent to email"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "404", description = "Email not found")
    })
    public ResponseEntity<Void> requestUnsubscribe(@Valid @RequestBody UnsubscribeRequest request) {
        service.requestUnsubscribe(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unsubscribe/confirm")
    @Operation(summary = "Confirm unsubscribe with verification code", description = "Unsubscribes the user after verifying the 6-digit code sent to their email.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Unsubscribed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid or expired code"),
        @ApiResponse(responseCode = "404", description = "Email not found")
    })
    public ResponseEntity<Void> confirmUnsubscribe(@Valid @RequestBody UnsubscribeConfirmRequest request) {
        service.confirmUnsubscribe(request.getEmail(), request.getCode());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MODERATOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List newsletter subscribers with pagination and search", description = "Accessible only by ADMIN or MODERATOR roles.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Subscribers retrieved successfully",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = PageResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden — requires ADMIN or MODERATOR role")
    })
    public ResponseEntity<PageResponse<NewsletterSubscriptionResponse>> getSubscribers(
            @Parameter(description = "Search term to match against name or email") @RequestParam(required = false) String search,
            @Parameter(description = "Page number (1-indexed)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page") @RequestParam(name = "per_page", defaultValue = "10") int perPage) {
        PageResponse<NewsletterSubscriptionResponse> response = service.getSubscribers(search, page, perPage);
        return ResponseEntity.ok(response);
    }
}
