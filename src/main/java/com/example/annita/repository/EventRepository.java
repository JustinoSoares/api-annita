package com.example.annita.repository;

import com.example.annita.model.Event;
import com.example.annita.model.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID>, JpaSpecificationExecutor<Event> {

    Page<Event> findByCreatedById(UUID userId, Pageable pageable);

    Optional<Event> findByIdAndStatus(UUID id, EventStatus status);

    long countByCreatedByIdAndStatus(UUID userId, EventStatus status);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
