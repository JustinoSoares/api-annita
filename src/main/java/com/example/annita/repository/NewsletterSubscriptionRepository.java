package com.example.annita.repository;

import com.example.annita.model.NewsletterSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NewsletterSubscriptionRepository extends JpaRepository<NewsletterSubscription, UUID> {

    Optional<NewsletterSubscription> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT n FROM NewsletterSubscription n WHERE " +
           "(:search IS NULL OR LOWER(n.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(n.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<NewsletterSubscription> findAllFiltered(@Param("search") String search, Pageable pageable);
}
