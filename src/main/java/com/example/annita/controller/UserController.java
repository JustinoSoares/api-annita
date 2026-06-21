package com.example.annita.controller;

import com.example.annita.dto.PageResponse;
import com.example.annita.dto.UserResponse;
import com.example.annita.dto.UserUpdateRequest;
import com.example.annita.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Endpoints for managing users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MODERATOR')")
    @Operation(summary = "List users with pagination, search, and filters", description = "Accessible only by ADMIN or MODERATOR roles.")
    public ResponseEntity<PageResponse<UserResponse>> getUsers(
            @Parameter(description = "Search term to match against username or email") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by user role") @RequestParam(required = false) com.example.annita.model.UserRole role,
            @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Page number (1-indexed)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page") @RequestParam(name = "per_page", defaultValue = "10") int perPage) {
        PageResponse<UserResponse> users = userService.getUsers(search, role, isActive, page, perPage);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MODERATOR') or principal.claims['userId'] == #id.toString()")
    @Operation(summary = "Get user by ID", description = "Accessible by ADMIN, MODERATOR, or the owner of the account.")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or principal.claims['userId'] == #id.toString()")
    @Operation(summary = "Update user details", description = "Accessible by ADMIN or the owner of the account.")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or principal.claims['userId'] == #id.toString()")
    @Operation(summary = "Delete user account", description = "Accessible by ADMIN or the owner of the account.")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
