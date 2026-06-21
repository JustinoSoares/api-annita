package com.example.annita.repository;

import com.example.annita.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByGroupName(String groupName);

    @Query("SELECT c FROM Category c WHERE " +
           "(:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.groupName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Category> findAllFiltered(@Param("search") String search, Pageable pageable);
}
