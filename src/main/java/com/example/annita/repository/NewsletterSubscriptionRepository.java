package com.example.annita.repository;

import com.example.annita.model.NewsletterSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NewsletterSubscriptionRepository extends JpaRepository<NewsletterSubscription, UUID>, JpaSpecificationExecutor<NewsletterSubscription> {

    Optional<NewsletterSubscription> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT DISTINCT s FROM NewsletterSubscription s LEFT JOIN s.subscriptionCategories sc LEFT JOIN sc.category c WHERE c.id = :categoryId OR sc IS NULL")
    List<NewsletterSubscription> findSubscribersForCategory(@Param("categoryId") UUID categoryId);
}
