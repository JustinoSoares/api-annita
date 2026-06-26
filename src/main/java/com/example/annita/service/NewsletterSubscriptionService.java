package com.example.annita.service;

import com.example.annita.dto.NewsletterSubscriptionResponse;
import com.example.annita.dto.PageResponse;
import com.example.annita.dto.SubscribeRequest;
import com.example.annita.model.NewsletterSubscription;
import com.example.annita.repository.NewsletterSubscriptionRepository;
import com.example.annita.repository.specification.NewsletterSubscriptionSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class NewsletterSubscriptionService {

    private final NewsletterSubscriptionRepository repository;
    private final EmailService emailService;

    public NewsletterSubscriptionService(NewsletterSubscriptionRepository repository, EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }

    public NewsletterSubscriptionResponse subscribe(SubscribeRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este email já está inscrito na newsletter");
        }

        NewsletterSubscription subscription = NewsletterSubscription.builder()
                .name(request.getName())
                .email(request.getEmail())
                .build();

        NewsletterSubscription saved = repository.save(subscription);
        return new NewsletterSubscriptionResponse(saved);
    }

    public void requestUnsubscribe(String email) {
        NewsletterSubscription subscription = repository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inscrição não encontrada para o email informado"));

        String code = String.format("%06d", new Random().nextInt(999999));
        subscription.setVerificationCode(code);
        subscription.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));

        repository.save(subscription);
        emailService.sendVerificationCode(email, code);
    }

    public void confirmUnsubscribe(String email, String code) {
        NewsletterSubscription subscription = repository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inscrição não encontrada para o email informado"));

        if (subscription.getVerificationCode() == null || subscription.getVerificationCodeExpiresAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhum código de verificação solicitado. Solicite um código de cancelamento primeiro.");
        }

        if (LocalDateTime.now().isAfter(subscription.getVerificationCodeExpiresAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código de verificação expirou. Solicite um novo código.");
        }

        if (!subscription.getVerificationCode().equals(code)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código de verificação inválido.");
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
