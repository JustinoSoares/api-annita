package com.example.annita.service;

import com.example.annita.dto.NotificationResponse;
import com.example.annita.dto.PageResponse;
import com.example.annita.model.Event;
import com.example.annita.model.Notification;
import com.example.annita.model.User;
import com.example.annita.repository.NotificationRepository;
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
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public PageResponse<NotificationResponse> getNotifications(UUID userId, int page, int perPage) {
        int pageIndex = Math.max(0, page - 1);
        int size = Math.max(1, perPage);
        PageRequest pageable = PageRequest.of(pageIndex, size);

        Page<Notification> notificationsPage = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<NotificationResponse> content = notificationsPage.getContent().stream()
                .map(NotificationResponse::new)
                .collect(Collectors.toList());

        return new PageResponse<>(notificationsPage, content);
    }

    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificação não encontrada"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Só pode marcar as suas próprias notificações");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        Page<Notification> page;
        int pageNumber = 0;
        do {
            PageRequest pageable = PageRequest.of(pageNumber, 100);
            page = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
            for (Notification notification : page.getContent()) {
                if (!notification.isRead()) {
                    notification.setRead(true);
                }
            }
            notificationRepository.saveAll(page.getContent());
            pageNumber++;
        } while (page.hasNext());
    }

    public void create(User user, Event event, String message) {
        Notification notification = Notification.builder()
                .user(user)
                .event(event)
                .message(message)
                .build();
        notificationRepository.save(notification);
    }
}
