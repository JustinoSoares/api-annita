package com.example.annita.dto;

import com.example.annita.model.Report;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ReportResponse {

    private UUID id;
    private UUID eventId;
    private String eventTitle;
    private String reason;
    private LocalDateTime createdAt;

    public ReportResponse(Report report) {
        this.id = report.getId();
        this.eventId = report.getEvent().getId();
        this.eventTitle = report.getEvent().getTitle();
        this.reason = report.getReason();
        this.createdAt = report.getCreatedAt();
    }
}
