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

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null && !auth.getName().isEmpty()) {
            return auth.getName();
        }
        return "system";
    }
}
