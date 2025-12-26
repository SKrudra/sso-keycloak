package com.skrudra.sso.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping({"/", "/home"})
    public String home() {
        return "Welcome — public home page!";
    }

    @GetMapping("/books")
    public List<String> books(Principal principal) {
        String name = (principal != null) ? principal.getName() : "anonymous";
        return List.of(
            "Effective Java — accessed by: " + name,
            "Clean Code",
            "Spring in Action"
        );
    }
}
