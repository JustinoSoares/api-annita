package com.example.annita.controller;

import com.example.annita.dto.MetricsResponse;
import com.example.annita.model.EventStatus;
import com.example.annita.repository.EventRepository;
import com.example.annita.repository.NewsletterSubscriptionRepository;
import com.example.annita.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics")
@Tag(name = "Metrics", description = "Public platform metrics")
public class MetricsController {

    private final EventRepository eventRepository;
    private final NewsletterSubscriptionRepository newsletterSubscriptionRepository;
    private final UserRepository userRepository;

    public MetricsController(EventRepository eventRepository,
                             NewsletterSubscriptionRepository newsletterSubscriptionRepository,
                             UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.newsletterSubscriptionRepository = newsletterSubscriptionRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    @Operation(summary = "Get public platform metrics", description = "Returns total events, newsletter subscribers, and active contributors. No authentication required.")
    public ResponseEntity<MetricsResponse> getMetrics() {
        MetricsResponse metrics = new MetricsResponse(
                eventRepository.countByStatus(EventStatus.APPROVED),
                newsletterSubscriptionRepository.count(),
                userRepository.countByIsActiveTrue()
        );
        return ResponseEntity.ok(metrics);
    }
}
