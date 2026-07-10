package com.example.annita.dto;

import com.example.annita.model.NewsletterSubscription;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
public class NewsletterSubscriptionResponse {
    private UUID id;
    private String name;
    private String email;
    private List<CategoryResponse> preferredCategories;
    private LocalDateTime createdAt;

    public NewsletterSubscriptionResponse(NewsletterSubscription subscription) {
        this.id = subscription.getId();
        this.name = subscription.getName();
        this.email = subscription.getEmail();
        this.createdAt = subscription.getCreatedAt();
        if (subscription.getSubscriptionCategories() != null && !subscription.getSubscriptionCategories().isEmpty()) {
            this.preferredCategories = subscription.getSubscriptionCategories().stream()
                    .map(sc -> new CategoryResponse(sc.getCategory()))
                    .collect(Collectors.toList());
        } else {
            this.preferredCategories = Collections.emptyList();
        }
    }
}
