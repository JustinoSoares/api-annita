package com.example.annita.dto;

import com.example.annita.model.EventModality;
import com.example.annita.model.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class EventRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @Size(max = 500, message = "Link must be at most 500 characters")
    private String link;

    @NotNull(message = "Category is required")
    private UUID categoryId;

    @NotNull(message = "Modality is required")
    private EventModality modality;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "Type is required")
    private EventType type;

    @Size(max = 500, message = "Cover image URL must be at most 500 characters")
    private String coverImage;
}
