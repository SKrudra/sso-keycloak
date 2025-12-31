package com.skrudra.sso.service;

import com.skrudra.sso.model.Book;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Service
public class BookService {

    private final ConcurrentMap<Long, Book> store = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    @PostConstruct
    public void init() {
        // Initialize with 5 sample books
        save(new Book(null, "Effective Java", "Joshua Bloch", 45.00));
        save(new Book(null, "Clean Code", "Robert C. Martin", 38.50));
        save(new Book(null, "Spring in Action", "Craig Walls", 42.75));
        save(new Book(null, "Domain-Driven Design", "Eric Evans", 55.00));
        save(new Book(null, "Refactoring", "Martin Fowler", 49.99));
    }

    public List<Book> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(store.values()));
    }

    public Optional<Book> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public Book save(Book book) {
        boolean isNew = (book.getId() == null);

        String user = getCurrentUsername();

        if (isNew) {
            book.setId(idGen.getAndIncrement());
            book.setCreatedBy(user);
            book.setCreatedDatetime(Instant.now());
        }
        // Always update updated info
        book.setUpdatedBy(user);
        book.setUpdatedDatetime(Instant.now());

        store.put(book.getId(), book);
        return book;
    }

    public void delete(Long id) {
        // mark who deleted (if book exists) before removing
        Book existing = store.get(id);
        String user = getCurrentUsername();
        if (existing != null) {
            existing.setUpdatedBy(user);
            existing.setUpdatedDatetime(Instant.now());
            store.put(id, existing);
        }
        store.remove(id);
    }

    private String firstNonEmpty(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return "system";

        // Prefer friendly display name if available
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String display = firstNonEmpty(jwt.getClaimAsString("preferred_username"), jwt.getClaimAsString("name"), jwt.getClaimAsString("email"), jwt.getSubject());
            if (display != null) return display;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof OidcUser oidc) {
            String display = firstNonEmpty((String) oidc.getClaimAsString("preferred_username"), (String) oidc.getClaimAsString("name"), (String) oidc.getClaimAsString("email"), oidc.getName());
            if (display != null) return display;
        }
        if (principal instanceof OAuth2User ou) {
            String display = firstNonEmpty((String) ou.getAttribute("preferred_username"), (String) ou.getAttribute("name"), (String) ou.getAttribute("email"), ou.getName());
            if (display != null) return display;
        }

        String name = auth.getName();
        return (name != null && !name.isBlank()) ? name : "system";
    }
}
