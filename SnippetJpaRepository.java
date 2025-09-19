package com.example.snippetshare.repository;

import com.example.snippetshare.entity.SnippetEntity;
import com.example.snippetshare.snippet.Language;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SnippetJpaRepository extends JpaRepository<SnippetEntity, UUID> {
    
    List<SnippetEntity> findAllByOrderByCreatedAtDesc();
    
    Page<SnippetEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    List<SnippetEntity> findByLanguageOrderByCreatedAtDesc(Language language);
    
    List<SnippetEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<SnippetEntity> findByUserIsNullOrderByCreatedAtDesc();
    
    List<SnippetEntity> findByIsSharedTrueOrderByCreatedAtDesc();
    
    List<SnippetEntity> findBySharedByUserIdOrderByCreatedAtDesc(UUID sharedByUserId);
    
    @Query("SELECT s FROM SnippetEntity s WHERE " +
           "LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.code) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY s.createdAt DESC")
    List<SnippetEntity> searchSnippets(@Param("query") String query);
}
