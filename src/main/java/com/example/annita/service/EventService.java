package com.example.annita.service;

import com.example.annita.dto.EventRequest;
import com.example.annita.dto.EventResponse;
import com.example.annita.dto.PageResponse;
import com.example.annita.model.*;
import com.example.annita.repository.CategoryRepository;
import com.example.annita.repository.EventRepository;
import com.example.annita.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public EventService(EventRepository eventRepository, CategoryRepository categoryRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public EventResponse create(EventRequest request, UUID userId) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));

        boolean hasReportedEvents = eventRepository.countByCreatedByIdAndStatus(userId, EventStatus.REPORTED) > 0;
        boolean autoApprove = user.getApprovedEventCount() >= 2 && !hasReportedEvents;

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .link(request.getLink())
                .category(category)
                .modality(request.getModality())
                .startDate(request.getStartDate())
                .type(request.getType())
                .coverImage(request.getCoverImage())
                .status(autoApprove ? EventStatus.APPROVED : EventStatus.PENDING)
                .createdBy(user)
                .build();

        Event saved = eventRepository.save(event);
        return new EventResponse(saved);
    }

    public PageResponse<EventResponse> getApproved(String search, UUID categoryId, EventModality modality, EventType type, int page, int perPage) {
        int pageIndex = Math.max(0, page - 1);
        int size = Math.max(1, perPage);
        PageRequest pageable = PageRequest.of(pageIndex, size);

        Page<Event> eventsPage = eventRepository.findApprovedFiltered(search, categoryId, modality, type, pageable);

        List<EventResponse> content = eventsPage.getContent().stream()
                .map(EventResponse::new)
                .collect(Collectors.toList());

        return new PageResponse<>(eventsPage, content);
    }

    public PageResponse<EventResponse> getAll(String search, UUID categoryId, EventModality modality, EventType type, EventStatus status, int page, int perPage) {
        int pageIndex = Math.max(0, page - 1);
        int size = Math.max(1, perPage);
        PageRequest pageable = PageRequest.of(pageIndex, size);

        Page<Event> eventsPage = eventRepository.findAllFiltered(search, categoryId, modality, type, status, pageable);

        List<EventResponse> content = eventsPage.getContent().stream()
                .map(EventResponse::new)
                .collect(Collectors.toList());

        return new PageResponse<>(eventsPage, content);
    }

    public EventResponse getById(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        return new EventResponse(event);
    }

    public EventResponse getApprovedById(UUID id) {
        Event event = eventRepository.findByIdAndStatus(id, EventStatus.APPROVED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        return new EventResponse(event);
    }

    public PageResponse<EventResponse> getByUser(UUID userId, int page, int perPage) {
        int pageIndex = Math.max(0, page - 1);
        int size = Math.max(1, perPage);
        PageRequest pageable = PageRequest.of(pageIndex, size);

        Page<Event> eventsPage = eventRepository.findByCreatedById(userId, pageable);

        List<EventResponse> content = eventsPage.getContent().stream()
                .map(EventResponse::new)
                .collect(Collectors.toList());

        return new PageResponse<>(eventsPage, content);
    }

    public EventResponse update(UUID id, EventRequest request, UUID userId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        if (!event.getCreatedBy().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own events");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found"));

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setLink(request.getLink());
        event.setCategory(category);
        event.setModality(request.getModality());
        event.setStartDate(request.getStartDate());
        event.setType(request.getType());
        event.setCoverImage(request.getCoverImage());

        Event updated = eventRepository.save(event);
        return new EventResponse(updated);
    }

    public void delete(UUID id, UUID userId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        if (!event.getCreatedBy().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own events");
        }

        eventRepository.deleteById(id);
    }

    public EventResponse approve(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        if (event.getStatus() != EventStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending events can be approved");
        }

        event.setStatus(EventStatus.APPROVED);
        Event saved = eventRepository.save(event);

        User creator = saved.getCreatedBy();
        creator.setApprovedEventCount(creator.getApprovedEventCount() + 1);
        userRepository.save(creator);

        return new EventResponse(saved);
    }

    public EventResponse reject(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        if (event.getStatus() != EventStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending events can be rejected");
        }

        event.setStatus(EventStatus.REJECTED);
        Event saved = eventRepository.save(event);

        return new EventResponse(saved);
    }

    public EventResponse report(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        if (event.getStatus() != EventStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only approved events can be reported");
        }

        event.setStatus(EventStatus.REPORTED);
        Event saved = eventRepository.save(event);

        return new EventResponse(saved);
    }
}
