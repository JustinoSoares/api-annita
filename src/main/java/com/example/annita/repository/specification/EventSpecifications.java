package com.example.annita.repository.specification;

import com.example.annita.model.Event;
import com.example.annita.model.EventModality;
import com.example.annita.model.EventStatus;
import com.example.annita.model.EventType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EventSpecifications {

    public static Specification<Event> approvedAndFiltered(String search, UUID categoryId, EventModality modality, EventType type) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("status"), EventStatus.APPROVED));

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (modality != null) {
                predicates.add(cb.equal(root.get("modality"), modality));
            }

            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Event> allFiltered(String search, UUID categoryId, EventModality modality, EventType type, EventStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (modality != null) {
                predicates.add(cb.equal(root.get("modality"), modality));
            }

            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
