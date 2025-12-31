package com.skrudra.sso.controller;

import java.security.Principal;
import java.util.List;

import com.skrudra.sso.model.Book;
import com.skrudra.sso.service.BookService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    private final BookService bookService;

    public HomeController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping({"/", "/home"})
    public String home() {
        return "Welcome — public home page!";
    }

    @GetMapping("/books")
    public List<Book> books(Principal principal) {
        // The endpoint is protected by security; return all books from the in-memory service
        return bookService.findAll();
    }
}
