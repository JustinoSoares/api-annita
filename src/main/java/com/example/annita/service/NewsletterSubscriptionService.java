package com.example.annita.service;

import com.example.annita.dto.NewsletterSubscriptionResponse;
import com.example.annita.dto.PageResponse;
import com.example.annita.dto.SubscribeRequest;
import com.example.annita.model.NewsletterSubscription;
import com.example.annita.repository.NewsletterSubscriptionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NewsletterSubscriptionService {

    private final NewsletterSubscriptionRepository repository;

    public NewsletterSubscriptionService(NewsletterSubscriptionRepository repository) {
        this.repository = repository;
    }

    public NewsletterSubscriptionResponse subscribe(SubscribeRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already subscribed to the newsletter");
        }

        NewsletterSubscription subscription = NewsletterSubscription.builder()
                .name(request.getName())
                .email(request.getEmail())
                .build();

        NewsletterSubscription saved = repository.save(subscription);
        return new NewsletterSubscriptionResponse(saved);
    }

    public void unsubscribe(String email) {
        NewsletterSubscription subscription = repository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found for the provided email"));
        repository.delete(subscription);
    }

    public PageResponse<NewsletterSubscriptionResponse> getSubscribers(String search, int page, int perPage) {
        int pageIndex = Math.max(0, page - 1);
        int size = Math.max(1, perPage);
        Pageable pageable = PageRequest.of(pageIndex, size);

        Page<NewsletterSubscription> pageResult = repository.findAllFiltered(search, pageable);

        List<NewsletterSubscriptionResponse> content = pageResult.getContent().stream()
                .map(NewsletterSubscriptionResponse::new)
                .collect(Collectors.toList());

        return new PageResponse<>(pageResult, content);
    }
}
