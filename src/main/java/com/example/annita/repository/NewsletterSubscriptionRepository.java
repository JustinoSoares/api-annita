package com.example.annita.repository;

import com.example.annita.model.NewsletterSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NewsletterSubscriptionRepository extends JpaRepository<NewsletterSubscription, UUID>, JpaSpecificationExecutor<NewsletterSubscription> {

    Optional<NewsletterSubscription> findByEmail(String email);

    boolean existsByEmail(String email);
}
