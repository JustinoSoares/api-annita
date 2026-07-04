package com.example.annita.dto;

import com.example.annita.model.Event;
import com.example.annita.model.EventModality;
import com.example.annita.model.EventStatus;
import com.example.annita.model.EventType;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class EventResponse {
    private UUID id;
    private String title;
    private String description;
    private String link;
    private CategoryResponse category;
    private EventModality modality;
    private LocalDateTime startDate;
    private EventType type;
    private String coverImage;
    private String location;
    private EventStatus status;
    private UUID createdById;
    private String createdByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EventResponse(Event event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.description = event.getDescription();
        this.link = event.getLink();
        this.category = new CategoryResponse(event.getCategory());
        this.modality = event.getModality();
        this.startDate = event.getStartDate();
        this.type = event.getType();
        this.coverImage = event.getCoverImage();
        this.location = event.getLocation();
        this.status = event.getStatus();
        this.createdById = event.getCreatedBy().getId();
        this.createdByUsername = event.getCreatedBy().getUsername();
        this.createdAt = event.getCreatedAt();
        this.updatedAt = event.getUpdatedAt();
    }
}
