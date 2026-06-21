package com.example.annita.controller;

import com.example.annita.dto.CategoryRequest;
import com.example.annita.dto.CategoryResponse;
import com.example.annita.dto.PageResponse;
import com.example.annita.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    @Operation(summary = "Create a new category", description = "Accessible only by ADMIN or MODERATOR roles.")
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List categories with pagination and search")
    public ResponseEntity<PageResponse<CategoryResponse>> getAll(
            @Parameter(description = "Search term to match against name or group") @RequestParam(required = false) String search,
            @Parameter(description = "Page number (1-indexed)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page") @RequestParam(name = "per_page", defaultValue = "10") int perPage) {
        PageResponse<CategoryResponse> response = categoryService.getAll(search, page, perPage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-group")
    @Operation(summary = "List categories by group name")
    public ResponseEntity<List<CategoryResponse>> getByGroup(@RequestParam String groupName) {
        List<CategoryResponse> response = categoryService.getByGroup(groupName);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<CategoryResponse> getById(@PathVariable UUID id) {
        CategoryResponse response = categoryService.getById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MODERATOR')")
    @Operation(summary = "Update a category", description = "Accessible only by ADMIN or MODERATOR roles.")
    public ResponseEntity<CategoryResponse> update(@PathVariable UUID id, @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MODERATOR')")
    @Operation(summary = "Delete a category", description = "Accessible only by ADMIN or MODERATOR roles.")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
