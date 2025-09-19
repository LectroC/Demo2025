package com.example.snippetshare.entity;

import com.example.snippetshare.snippet.Language;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "snippets")
public class SnippetEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String title;
    
    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String code;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private UserEntity user;
    
    @Column(name = "is_shared", nullable = false)
    private Boolean isShared = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_by_user_id")
    @JsonIgnore
    private UserEntity sharedByUser;
    
    // Constructors
    public SnippetEntity() {}
    
    public SnippetEntity(String title, String code, Language language) {
        this.title = title;
        this.code = code;
        this.language = language;
    }
    
    public SnippetEntity(String title, String code, Language language, UserEntity user) {
        this.title = title;
        this.code = code;
        this.language = language;
        this.user = user;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public Language getLanguage() {
        return language;
    }
    
    public void setLanguage(Language language) {
        this.language = language;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public UserEntity getUser() {
        return user;
    }
    
    public void setUser(UserEntity user) {
        this.user = user;
    }
    
    public Boolean getIsShared() {
        return isShared;
    }
    
    public void setIsShared(Boolean isShared) {
        this.isShared = isShared;
    }
    
    public UserEntity getSharedByUser() {
        return sharedByUser;
    }
    
    public void setSharedByUser(UserEntity sharedByUser) {
        this.sharedByUser = sharedByUser;
    }
}
