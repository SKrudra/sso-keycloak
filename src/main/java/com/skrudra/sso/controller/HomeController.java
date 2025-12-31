package com.skrudra.sso.controller;

import java.security.Principal;
import java.util.List;

import com.skrudra.sso.model.Book;
import com.skrudra.sso.service.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final BookService bookService;

    public HomeController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping({"/", "/home"})
    public String home(Principal principal, Model model) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "home"; // render templates/home.html
    }

    @GetMapping("/books")
    public String books(Principal principal, Model model) {
        List<Book> books = bookService.findAll();
        model.addAttribute("books", books);
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "books"; // render templates/books.html
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // render templates/login.html
    }
}
