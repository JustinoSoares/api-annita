package com.example.annita.repository;

import com.example.annita.model.NewsletterSubscriptionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NewsletterSubscriptionCategoryRepository extends JpaRepository<NewsletterSubscriptionCategory, UUID> {
}
