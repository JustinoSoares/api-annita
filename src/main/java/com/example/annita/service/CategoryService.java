package com.example.annita.service;

import com.example.annita.dto.CategoryRequest;
import com.example.annita.dto.CategoryResponse;
import com.example.annita.dto.PageResponse;
import com.example.annita.model.Category;
import com.example.annita.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponse create(CategoryRequest request) {
        Category category = Category.builder()
                .name(request.getName())
                .groupName(request.getGroupName())
                .build();

        Category saved = categoryRepository.save(category);
        return new CategoryResponse(saved);
    }

    public PageResponse<CategoryResponse> getAll(String search, int page, int perPage) {
        int pageIndex = Math.max(0, page - 1);
        int size = Math.max(1, perPage);
        PageRequest pageable = PageRequest.of(pageIndex, size);

        Page<Category> categoriesPage = categoryRepository.findAllFiltered(search, pageable);

        List<CategoryResponse> content = categoriesPage.getContent().stream()
                .map(CategoryResponse::new)
                .collect(Collectors.toList());

        return new PageResponse<>(categoriesPage, content);
    }

    public Map<String, List<CategoryResponse>> getGrouped() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::new)
                .collect(Collectors.groupingBy(CategoryResponse::getGroupName, LinkedHashMap::new, Collectors.toList()));
    }

    public CategoryResponse getById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        return new CategoryResponse(category);
    }

    public CategoryResponse update(UUID id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        category.setName(request.getName());
        category.setGroupName(request.getGroupName());

        Category updated = categoryRepository.save(category);
        return new CategoryResponse(updated);
    }

    public void delete(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
        }
        categoryRepository.deleteById(id);
    }
}
