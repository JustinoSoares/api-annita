package com.example.annita.repository;

import com.example.annita.model.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

    List<Report> findByEventId(UUID eventId);

    long countByEventId(UUID eventId);

    Page<Report> findByReportedById(UUID userId, Pageable pageable);

    void deleteByEventId(UUID eventId);
}
