package com.example.annita.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminDashboardResponse {

    private MetricItem events;
    private MetricItem users;
    private MetricItem categories;
    private MetricItem subscribers;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class MetricItem {
        private long total;
        private long currentMonth;
        private long lastMonth;
        private double changePercentage;
    }
}
