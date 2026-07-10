package com.example.annita.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "tb_newsletter_subscription_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsletterSubscriptionCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private NewsletterSubscription subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
