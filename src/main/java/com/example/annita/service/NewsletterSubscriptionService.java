package com.example.annita.service;

import com.example.annita.dto.NewsletterSubscriptionResponse;
import com.example.annita.dto.PageResponse;
import com.example.annita.dto.SubscribeRequest;
import com.example.annita.model.Category;
import com.example.annita.model.NewsletterSubscription;
import com.example.annita.repository.CategoryRepository;
import com.example.annita.repository.NewsletterSubscriptionRepository;
import com.example.annita.repository.specification.NewsletterSubscriptionSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class NewsletterSubscriptionService {

    private final NewsletterSubscriptionRepository repository;
    private final EmailService emailService;
    private final CategoryRepository categoryRepository;

    public NewsletterSubscriptionService(NewsletterSubscriptionRepository repository, EmailService emailService, CategoryRepository categoryRepository) {
        this.repository = repository;
        this.emailService = emailService;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public NewsletterSubscriptionResponse subscribe(SubscribeRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este email já está inscrito na newsletter");
        }

        NewsletterSubscription subscription = NewsletterSubscription.builder()
                .name(request.getName())
                .email(request.getEmail())
                .build();

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
            if (categories.size() != request.getCategoryIds().size()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uma ou mais categorias não encontradas");
            }
            subscription.setPreferredCategories(categories);
        }

        NewsletterSubscription saved = repository.save(subscription);
        return new NewsletterSubscriptionResponse(saved);
    }

    public void requestUnsubscribe(String email) {
        NewsletterSubscription subscription = repository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Não foi encontrada uma inscrição com este email"));

        String code = String.format("%06d", new Random().nextInt(999999));
        subscription.setVerificationCode(code);
        subscription.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));

        repository.save(subscription);
        emailService.sendVerificationCode(email, code);
    }

    public void confirmUnsubscribe(String email, String code) {
        NewsletterSubscription subscription = repository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Não foi encontrada uma inscrição com este email"));

        if (subscription.getVerificationCode() == null || subscription.getVerificationCodeExpiresAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ainda não pediu um código de verificação. Peça um código primeiro.");
        }

        if (LocalDateTime.now().isAfter(subscription.getVerificationCodeExpiresAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O código de verificação expirou. Peça um novo código.");
        }

        if (!subscription.getVerificationCode().equals(code)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O código de verificação está incorreto.");
        }

        repository.delete(subscription);
    }

    public PageResponse<NewsletterSubscriptionResponse> getSubscribers(String search, int page, int perPage) {
        int pageIndex = Math.max(0, page - 1);
        int size = Math.max(1, perPage);
        Pageable pageable = PageRequest.of(pageIndex, size);

        Page<NewsletterSubscription> pageResult = repository.findAll(NewsletterSubscriptionSpecifications.filter(search), pageable);

        List<NewsletterSubscriptionResponse> content = pageResult.getContent().stream()
                .map(NewsletterSubscriptionResponse::new)
                .collect(Collectors.toList());

        return new PageResponse<>(pageResult, content);
    }
}
