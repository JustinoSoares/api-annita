package com.example.annita.dto;

import com.example.annita.model.Notification;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class NotificationResponse {

    private UUID id;
    private UUID eventId;
    private String eventTitle;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;

    public NotificationResponse(Notification notification) {
        this.id = notification.getId();
        this.message = notification.getMessage();
        this.isRead = notification.isRead();
        this.createdAt = notification.getCreatedAt();
        if (notification.getEvent() != null) {
            this.eventId = notification.getEvent().getId();
            this.eventTitle = notification.getEvent().getTitle();
        }
    }
}
