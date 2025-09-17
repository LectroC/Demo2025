package com.example.snippetshare.snippet;

import com.example.snippetshare.entity.SnippetEntity;
import com.example.snippetshare.repository.SnippetJpaRepository;
import com.example.snippetshare.repository.UserJpaRepository;
import com.example.snippetshare.entity.UserEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class SnippetService {

    private final SnippetJpaRepository repository;
    private final UserJpaRepository userRepository;

    public SnippetService(SnippetJpaRepository repository, UserJpaRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public SnippetEntity create(@Valid CreateSnippetRequest request, String userNameOrNull) {
        SnippetEntity snippet = new SnippetEntity(request.title(), request.code(), request.language());
        if (userNameOrNull != null && !userNameOrNull.isBlank()) {
            userRepository.findByNameIgnoreCase(userNameOrNull)
                    .ifPresent(snippet::setUser);
        }
        return repository.save(snippet);
    }

    public Optional<SnippetEntity> get(UUID id) {
        return repository.findById(id);
    }

    public List<SnippetEntity> list() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public List<SnippetEntity> listGuest() {
        return repository.findByUserIsNullOrderByCreatedAtDesc();
    }

    public List<SnippetEntity> listForUserName(String name) {
        return userRepository.findByNameIgnoreCase(name)
                .map(UserEntity::getId)
                .map(repository::findByUserIdOrderByCreatedAtDesc)
                .orElseGet(List::of);
    }

    public boolean delete(UUID id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    public record CreateSnippetRequest(
            @NotBlank String title,
            @NotBlank String code,
            @NotNull Language language
    ) {}
}


