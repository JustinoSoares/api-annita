package com.example.annita.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MetricsResponse {
    private long totalEvents;
    private long totalNewsletterSubscribers;
    private long activeContributors;
}
