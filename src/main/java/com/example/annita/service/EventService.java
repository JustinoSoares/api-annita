package com.example.annita.service;

import com.example.annita.dto.EventRequest;
import com.example.annita.dto.EventResponse;
import com.example.annita.dto.PageResponse;
import com.example.annita.dto.ReportRequest;
import com.example.annita.dto.VoteRequest;
import com.example.annita.model.*;
import com.example.annita.repository.CategoryRepository;
import com.example.annita.repository.EventRepository;
import com.example.annita.repository.EventVoteRepository;
import com.example.annita.repository.ReportRepository;
import com.example.annita.repository.UserRepository;
import com.example.annita.repository.specification.EventSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final ReportRepository reportRepository;
    private final EventVoteRepository eventVoteRepository;
    private final NotificationService notificationService;

    public EventService(EventRepository eventRepository, CategoryRepository categoryRepository, UserRepository userRepository, EmailService emailService, ReportRepository reportRepository, EventVoteRepository eventVoteRepository, NotificationService notificationService) {
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.reportRepository = reportRepository;
        this.eventVoteRepository = eventVoteRepository;
        this.notificationService = notificationService;
    }

    public PageResponse<EventResponse> getEvents(String search, UUID categoryId, EventModality modality, EventType type, EventStatus status, UUID userId, String role, int page, int perPage) {
        if ("ADMIN".equals(role) || "MODERATOR".equals(role)) {
            return getAll(search, categoryId, modality, type, status, page, perPage, userId);
        }
        return getApproved(search, categoryId, modality, type, page, perPage, userId);
    }

    public EventResponse create(EventRequest request, UUID userId) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria não encontrada"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário não encontrado"));

        long pendingCount = eventRepository.countByCreatedByIdAndStatus(userId, EventStatus.PENDING);
        if (pendingCount > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Você já possui um evento pendente de aprovação. Aguarde a aprovação antes de criar outro.");
        }

        boolean hasReportedEvents = eventRepository.countByCreatedByIdAndStatus(userId, EventStatus.REPORTED) > 0;
        boolean autoApprove = user.getApprovedEventCount() >= 1 && !hasReportedEvents;

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .link(request.getLink())
                .category(category)
                .modality(request.getModality())
                .startDate(request.getStartDate())
                .type(request.getType())
                .coverImage(request.getCoverImage())
                .location(request.getLocation())
                .status(autoApprove ? EventStatus.APPROVED : EventStatus.PENDING)
                .createdBy(user)
                .build();

        Event saved = eventRepository.save(event);
        return buildResponse(saved, userId);
    }

    public PageResponse<EventResponse> getApproved(String search, UUID categoryId, EventModality modality, EventType type, int page, int perPage) {
        return getApproved(search, categoryId, modality, type, page, perPage, null);
    }

    public PageResponse<EventResponse> getApproved(String search, UUID categoryId, EventModality modality, EventType type, int page, int perPage, UUID userId) {
        int pageIndex = Math.max(0, page - 1);
        int size = Math.max(1, perPage);
        PageRequest pageable = PageRequest.of(pageIndex, size);

        Page<Event> eventsPage = eventRepository.findAll(EventSpecifications.approvedAndFiltered(search, categoryId, modality, type), pageable);

        List<EventResponse> content = eventsPage.getContent().stream()
                .map(e -> buildResponse(e, userId))
                .collect(Collectors.toList());

        return new PageResponse<>(eventsPage, content);
    }

    public PageResponse<EventResponse> getAll(String search, UUID categoryId, EventModality modality, EventType type, EventStatus status, int page, int perPage) {
        return getAll(search, categoryId, modality, type, status, page, perPage, null);
    }

    public PageResponse<EventResponse> getAll(String search, UUID categoryId, EventModality modality, EventType type, EventStatus status, int page, int perPage, UUID userId) {
        int pageIndex = Math.max(0, page - 1);
        int size = Math.max(1, perPage);
        PageRequest pageable = PageRequest.of(pageIndex, size);

        Page<Event> eventsPage = eventRepository.findAll(EventSpecifications.allFiltered(search, categoryId, modality, type, status), pageable);

        List<EventResponse> content = eventsPage.getContent().stream()
                .map(e -> buildResponse(e, userId))
                .collect(Collectors.toList());

        return new PageResponse<>(eventsPage, content);
    }

    public EventResponse getById(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado"));
        return buildResponse(event, null);
    }

    public EventResponse getById(UUID id, UUID userId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado"));
        return buildResponse(event, userId);
    }

    public EventResponse getApprovedById(UUID id) {
        Event event = eventRepository.findByIdAndStatus(id, EventStatus.APPROVED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado"));
        return buildResponse(event, null);
    }

    public EventResponse getApprovedById(UUID id, UUID userId) {
        Event event = eventRepository.findByIdAndStatus(id, EventStatus.APPROVED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado"));
        return buildResponse(event, userId);
    }

    public PageResponse<EventResponse> getByUser(UUID userId, int page, int perPage) {
        return getByUser(userId, page, perPage, null);
    }

    public PageResponse<EventResponse> getByUser(UUID userId, int page, int perPage, UUID viewerId) {
        int pageIndex = Math.max(0, page - 1);
        int size = Math.max(1, perPage);
        PageRequest pageable = PageRequest.of(pageIndex, size);

        Page<Event> eventsPage = eventRepository.findByCreatedById(userId, pageable);

        List<EventResponse> content = eventsPage.getContent().stream()
                .map(e -> buildResponse(e, viewerId))
                .collect(Collectors.toList());

        return new PageResponse<>(eventsPage, content);
    }

    public EventResponse update(UUID id, EventRequest request, UUID userId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado"));

        if (!event.getCreatedBy().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Só pode editar os seus próprios eventos");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria não encontrada"));

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setLink(request.getLink());
        event.setCategory(category);
        event.setModality(request.getModality());
        event.setStartDate(request.getStartDate());
        event.setType(request.getType());
        event.setCoverImage(request.getCoverImage());
        event.setLocation(request.getLocation());

        Event updated = eventRepository.save(event);
        return buildResponse(updated, userId);
    }

    public void delete(UUID id, UUID userId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado"));

        if (!event.getCreatedBy().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Só pode excluir os seus próprios eventos");
        }

        eventRepository.deleteById(id);
    }

    public EventResponse approve(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado"));

        if (event.getStatus() != EventStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Apenas eventos pendentes podem ser aprovados");
        }

        event.setStatus(EventStatus.APPROVED);
        Event saved = eventRepository.save(event);

        User creator = saved.getCreatedBy();
        creator.setApprovedEventCount(creator.getApprovedEventCount() + 1);
        userRepository.save(creator);

        notificationService.create(creator, saved, "Seu evento \"" + saved.getTitle() + "\" foi aprovado!");

        return buildResponse(saved, null);
    }

    public EventResponse reject(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado"));

        if (event.getStatus() != EventStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Apenas eventos pendentes podem ser rejeitados");
        }

        event.setStatus(EventStatus.REJECTED);
        Event saved = eventRepository.save(event);

        notificationService.create(saved.getCreatedBy(), saved, "Seu evento \"" + saved.getTitle() + "\" foi rejeitado.");

        return buildResponse(saved, null);
    }

    public EventResponse report(UUID id, ReportRequest request, UUID userId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado"));

        if (event.getStatus() != EventStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Apenas eventos aprovados podem ser denunciados");
        }

        User reporter = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário não encontrado"));

        Report report = Report.builder()
                .event(event)
                .reportedBy(reporter)
                .reason(request.getReason())
                .build();

        reportRepository.save(report);

        long reportCount = reportRepository.countByEventId(id);
        Event saved = event;
        if (reportCount >= 3) {
            event.setStatus(EventStatus.REPORTED);
            saved = eventRepository.save(event);
            User creator = saved.getCreatedBy();
            emailService.sendEventReportedNotification(creator.getEmail(), saved.getTitle());
            notificationService.create(creator, saved, "Seu evento \"" + saved.getTitle() + "\" foi removido por denúncias.");
        }

        return buildResponse(saved, userId);
    }

    public EventResponse vote(UUID eventId, VoteRequest request, UUID userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado"));

        if (event.getStatus() != EventStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Apenas eventos aprovados podem receber votos");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário não encontrado"));

        Optional<EventVote> existingVote = eventVoteRepository.findByEventIdAndUserId(eventId, userId);

        if (existingVote.isPresent()) {
            EventVote vote = existingVote.get();
            if (vote.getType() == request.getType()) {
                eventVoteRepository.delete(vote);
            } else {
                vote.setType(request.getType());
                eventVoteRepository.save(vote);
            }
        } else {
            EventVote vote = EventVote.builder()
                    .event(event)
                    .user(user)
                    .type(request.getType())
                    .build();
            eventVoteRepository.save(vote);
        }

        return buildResponse(event, userId);
    }

    public EventResponse unvote(UUID eventId, UUID userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado"));

        eventVoteRepository.deleteByEventIdAndUserId(eventId, userId);

        return buildResponse(event, userId);
    }

    private EventResponse buildResponse(Event event, UUID userId) {
        EventResponse response = new EventResponse(event);
        response.setUpvoteCount(eventVoteRepository.countByEventIdAndType(event.getId(), VoteType.UPVOTE));
        response.setDownvoteCount(eventVoteRepository.countByEventIdAndType(event.getId(), VoteType.DOWNVOTE));
        if (userId != null) {
            eventVoteRepository.findByEventIdAndUserId(event.getId(), userId)
                    .ifPresent(vote -> response.setUserVote(vote.getType()));
        }
        return response;
    }
}
