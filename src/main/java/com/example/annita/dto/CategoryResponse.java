package com.example.annita.dto;

import com.example.annita.model.Category;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class CategoryResponse {
    private UUID id;
    private String name;
    private String groupName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CategoryResponse(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.groupName = category.getGroupName();
        this.createdAt = category.getCreatedAt();
        this.updatedAt = category.getUpdatedAt();
    }
}
