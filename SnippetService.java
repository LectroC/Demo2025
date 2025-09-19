package com.example.snippetshare.snippet;

import com.example.snippetshare.entity.SnippetEntity;
import com.example.snippetshare.repository.SnippetJpaRepository;
import com.example.snippetshare.repository.SnippetRecipientRepository;
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
    private final SnippetRecipientRepository recipientRepository;

    public SnippetService(SnippetJpaRepository repository, UserJpaRepository userRepository, SnippetRecipientRepository recipientRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.recipientRepository = recipientRepository;
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

    public SnippetEntity share(@Valid CreateSnippetRequest request, String userNameOrNull) {
        SnippetEntity snippet = new SnippetEntity(request.title(), request.code(), request.language());
        snippet.setIsShared(true);
        
        if (userNameOrNull != null && !userNameOrNull.isBlank()) {
            userRepository.findByNameIgnoreCase(userNameOrNull)
                    .ifPresent(snippet::setSharedByUser);
        }
        return repository.save(snippet);
    }

    public List<SnippetEntity> listShared() {
        return repository.findByIsSharedTrueOrderByCreatedAtDesc();
    }

    public List<SnippetEntity> listSharedForUser(String userName) {
        return userRepository.findByNameIgnoreCase(userName)
                .map(UserEntity::getId)
                .map(repository::findBySharedByUserIdOrderByCreatedAtDesc)
                .orElseGet(List::of);
    }

    public Optional<SnippetEntity> update(UUID id, @Valid CreateSnippetRequest request) {
        return repository.findById(id)
                .map(snippet -> {
                    snippet.setTitle(request.title());
                    snippet.setCode(request.code());
                    snippet.setLanguage(request.language());
                    return repository.save(snippet);
                });
    }

    public void shareToRecipients(UUID snippetId, List<String> recipientUserNames) {
        var snippetOpt = repository.findById(snippetId);
        if (snippetOpt.isEmpty()) return;
        var snippet = snippetOpt.get();
        // mark as shared and set sender if missing
        snippet.setIsShared(true);
        if (snippet.getSharedByUser() == null && snippet.getUser() != null) {
            snippet.setSharedByUser(snippet.getUser());
        }
        repository.save(snippet);

        for (String name : recipientUserNames) {
            userRepository.findByNameIgnoreCase(name).ifPresent(user -> {
                recipientRepository.save(new com.example.snippetshare.entity.SnippetRecipient(snippet, user));
            });
        }
    }

    public List<SnippetEntity> listSharedWithUser(String userName) {
        return userRepository.findByNameIgnoreCase(userName)
                .map(user -> recipientRepository.findByUser_Id(user.getId()))
                .stream()
                .flatMap(List::stream)
                .map(rec -> rec.getSnippet())
                .toList();
    }

    public record CreateSnippetRequest(
            @NotBlank String title,
            @NotBlank String code,
            @NotNull Language language
    ) {}
}


