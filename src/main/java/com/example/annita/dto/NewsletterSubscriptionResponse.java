package com.example.annita.dto;

import com.example.annita.model.NewsletterSubscription;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class NewsletterSubscriptionResponse {
    private UUID id;
    private String name;
    private String email;
    private LocalDateTime createdAt;

    public NewsletterSubscriptionResponse(NewsletterSubscription subscription) {
        this.id = subscription.getId();
        this.name = subscription.getName();
        this.email = subscription.getEmail();
        this.createdAt = subscription.getCreatedAt();
    }
}
