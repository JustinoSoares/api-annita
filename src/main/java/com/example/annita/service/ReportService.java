package com.example.annita.service;

import com.example.annita.dto.PageResponse;
import com.example.annita.dto.ReportResponse;
import com.example.annita.model.Report;
import com.example.annita.repository.ReportRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
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
}
