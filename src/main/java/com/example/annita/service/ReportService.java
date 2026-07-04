package com.example.annita.service;

import com.example.annita.dto.PageResponse;
import com.example.annita.dto.ReportResponse;
import com.example.annita.model.Event;
import com.example.annita.model.EventStatus;
import com.example.annita.model.Report;
import com.example.annita.repository.EventRepository;
import com.example.annita.repository.ReportRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final EventRepository eventRepository;

    public ReportService(ReportRepository reportRepository, EventRepository eventRepository) {
        this.reportRepository = reportRepository;
        this.eventRepository = eventRepository;
    }

    public PageResponse<ReportResponse> getReportsByUser(UUID userId, int page, int perPage) {
        int pageIndex = Math.max(0, page - 1);
        int size = Math.max(1, perPage);
        PageRequest pageable = PageRequest.of(pageIndex, size);

        Page<Report> reportsPage = reportRepository.findByReportedById(userId, pageable);

        List<ReportResponse> content = reportsPage.getContent().stream()
                .map(ReportResponse::new)
                .collect(Collectors.toList());

        return new PageResponse<>(reportsPage, content);
    }

    @Transactional
    public void removeReport(UUID reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Denúncia não encontrada"));

        Event event = report.getEvent();
        reportRepository.delete(report);

        if (event.getStatus() == EventStatus.REPORTED) {
            long remaining = reportRepository.countByEventId(event.getId());
            if (remaining < 3) {
                event.setStatus(EventStatus.APPROVED);
                eventRepository.save(event);
            }
        }
    }
}
