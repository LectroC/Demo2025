package com.example.snippetshare.snippet;

import com.example.snippetshare.entity.SnippetEntity;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/snippets")
public class SnippetController {

    private static final Logger logger = LoggerFactory.getLogger(SnippetController.class);
    private final SnippetService service;

    public SnippetController(SnippetService service) {
        this.service = service;
    }

    @GetMapping
    public List<SnippetEntity> list() {
        return service.list();
    }

    @GetMapping("/guest")
    public List<SnippetEntity> listGuest() {
        return service.listGuest();
    }

    @GetMapping("/me")
    public ResponseEntity<List<SnippetEntity>> listMe(@RequestHeader(value = "X-User-Name", required = false) String userName) {
        if (userName == null || userName.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok(service.listForUserName(userName));
    }

    @GetMapping("/languages")
    public List<Language> languages() {
        return Arrays.asList(Language.values());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SnippetEntity> get(@PathVariable("id") UUID id) {
        return service.get(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SnippetEntity create(@RequestHeader(value = "X-User-Name", required = false) String userName,
                                @Valid @RequestBody SnippetService.CreateSnippetRequest request) {
        return service.create(request, userName);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        logger.info("Attempting to delete snippet with ID: {}", id);
        boolean removed = service.delete(id);
        if (removed) {
            logger.info("Successfully deleted snippet with ID: {}", id);
            return ResponseEntity.noContent().build();
        } else {
            logger.warn("Failed to delete snippet with ID: {} - snippet not found", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/share")
    @ResponseStatus(HttpStatus.CREATED)
    public SnippetEntity share(@RequestHeader(value = "X-User-Name", required = false) String userName,
                              @Valid @RequestBody SnippetService.CreateSnippetRequest request) {
        return service.share(request, userName);
    }

    @GetMapping("/shared")
    public List<SnippetDto> listShared() {
        return service.listShared().stream()
                .map(SnippetDto::fromEntity)
                .toList();
    }

    @GetMapping("/shared/me")
    public ResponseEntity<List<SnippetDto>> listSharedForUser(@RequestHeader(value = "X-User-Name", required = false) String userName) {
        if (userName == null || userName.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        List<SnippetDto> sharedSnippets = service.listSharedWithUser(userName).stream()
                .map(SnippetDto::fromEntity)
                .toList();
        return ResponseEntity.ok(sharedSnippets);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SnippetEntity> update(@PathVariable("id") UUID id,
                                               @Valid @RequestBody SnippetService.CreateSnippetRequest request) {
        return service.update(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public record ShareToUsersRequest(List<String> userNames) {}

    @PostMapping("/{id}/share-to")
    public ResponseEntity<Void> shareToUsers(@PathVariable("id") UUID id,
                                             @RequestBody ShareToUsersRequest request) {
        if (request == null || request.userNames() == null || request.userNames().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        service.shareToRecipients(id, request.userNames());
        return ResponseEntity.noContent().build();
    }
}


