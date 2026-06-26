package com.example.annita.controller;

import com.example.annita.dto.CategoryRequest;
import com.example.annita.dto.CategoryResponse;
import com.example.annita.dto.PageResponse;
import com.example.annita.service.CategoryService;
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

import java.util.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Endpoints for managing event categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MODERATOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a new category", description = "Accessible only by ADMIN or MODERATOR roles.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Category created successfully",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = CategoryResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "403", description = "Forbidden — requires ADMIN or MODERATOR role")
    })
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List categories with pagination and search")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categories retrieved successfully",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = PageResponse.class)))
    })
    public ResponseEntity<PageResponse<CategoryResponse>> getAll(
            @Parameter(description = "Search term to match against name or group") @RequestParam(required = false) String search,
            @Parameter(description = "Page number (1-indexed)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page") @RequestParam(name = "per_page", defaultValue = "10") int perPage) {
        PageResponse<CategoryResponse> response = categoryService.getAll(search, page, perPage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-group")
    @Operation(summary = "List all categories grouped by group name")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categories grouped by group name",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    public ResponseEntity<Map<String, List<CategoryResponse>>> getGrouped() {
        Map<String, List<CategoryResponse>> response = categoryService.getGrouped();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category found",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = CategoryResponse.class))),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<CategoryResponse> getById(@PathVariable UUID id) {
        CategoryResponse response = categoryService.getById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MODERATOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update a category", description = "Accessible only by ADMIN or MODERATOR roles.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category updated successfully",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = CategoryResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "403", description = "Forbidden — requires ADMIN or MODERATOR role"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<CategoryResponse> update(@PathVariable UUID id, @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MODERATOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete a category", description = "Accessible only by ADMIN or MODERATOR roles.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden — requires ADMIN or MODERATOR role"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "409", description = "Category has dependencies")
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
