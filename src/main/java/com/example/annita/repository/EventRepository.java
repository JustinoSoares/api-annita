package com.example.annita.repository;

import com.example.annita.model.Event;
import com.example.annita.model.EventModality;
import com.example.annita.model.EventStatus;
import com.example.annita.model.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    Page<Event> findByCreatedById(UUID userId, Pageable pageable);

    Optional<Event> findByIdAndStatus(UUID id, EventStatus status);

    @Query("SELECT e FROM Event e WHERE e.status = 'APPROVED' AND " +
           "(:search IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:categoryId IS NULL OR e.category.id = :categoryId) " +
           "AND (:modality IS NULL OR e.modality = :modality) " +
           "AND (:type IS NULL OR e.type = :type)")
    Page<Event> findApprovedFiltered(
            @Param("search") String search,
            @Param("categoryId") UUID categoryId,
            @Param("modality") EventModality modality,
            @Param("type") EventType type,
            Pageable pageable);

    @Query("SELECT e FROM Event e WHERE " +
           "(:search IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:categoryId IS NULL OR e.category.id = :categoryId) " +
           "AND (:modality IS NULL OR e.modality = :modality) " +
           "AND (:type IS NULL OR e.type = :type) " +
           "AND (:status IS NULL OR e.status = :status)")
    Page<Event> findAllFiltered(
            @Param("search") String search,
            @Param("categoryId") UUID categoryId,
            @Param("modality") EventModality modality,
            @Param("type") EventType type,
            @Param("status") EventStatus status,
            Pageable pageable);

    long countByCreatedByIdAndStatus(UUID userId, EventStatus status);
}
