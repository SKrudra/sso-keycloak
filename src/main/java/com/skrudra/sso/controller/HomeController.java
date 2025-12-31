package com.skrudra.sso.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import com.skrudra.sso.model.Book;
import com.skrudra.sso.service.BookService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@Controller
public class HomeController {

    private final BookService bookService;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final ClientRegistrationRepository clientRegistrationRepository;

    public HomeController(BookService bookService,
                          OAuth2AuthorizedClientService authorizedClientService,
                          ClientRegistrationRepository clientRegistrationRepository) {
        this.bookService = bookService;
        this.authorizedClientService = authorizedClientService;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @GetMapping({"/", "/home"})
    public String home(Principal principal, Model model) {
        // Determine a friendly display name for the current user and expose as 'displayName'
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String displayName = null;
        Map<String, Object> tokenClaims = null;

        if (auth != null) {
            // Try JWT
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();
                tokenClaims = jwt.getClaims();
                displayName = firstNonEmpty(jwt.getClaimAsString("preferred_username"), jwt.getClaimAsString("name"), jwt.getClaimAsString("email"), jwt.getSubject());
            } else if (auth.getPrincipal() instanceof OidcUser oidcUser) {
                tokenClaims = oidcUser.getIdToken().getClaims();
                displayName = firstNonEmpty((String) oidcUser.getClaimAsString("preferred_username"), (String) oidcUser.getClaimAsString("name"), (String) oidcUser.getClaimAsString("email"), oidcUser.getName());
            } else if (auth.getPrincipal() instanceof OAuth2User oauth2User) {
                tokenClaims = oauth2User.getAttributes();
                displayName = firstNonEmpty((String) oauth2User.getAttribute("preferred_username"), (String) oauth2User.getAttribute("name"), (String) oauth2User.getAttribute("login"), (String) oauth2User.getAttribute("email"), oauth2User.getName());
            }
        }

        if (displayName == null && principal != null) {
            displayName = principal.getName();
        }

        if (displayName != null) {
            model.addAttribute("displayName", displayName);
        }

        if (tokenClaims != null && !tokenClaims.isEmpty()) {
            model.addAttribute("tokenClaims", tokenClaims);
        }

        // --- load authorized client to expose tokens (access/id/refresh) if available ---
        String registrationId = "keycloak";
        String principalName = auth != null ? auth.getName() : (principal != null ? principal.getName() : null);
        if (principalName != null) {
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(registrationId, principalName);
            if (client != null) {
                Map<String, Object> tokens = new java.util.LinkedHashMap<>();
                if (client.getAccessToken() != null) {
                    tokens.put("access_token", client.getAccessToken().getTokenValue());
                    tokens.put("access_token_expires_at", client.getAccessToken().getExpiresAt());
                }
                if (client.getRefreshToken() != null) {
                    tokens.put("refresh_token", client.getRefreshToken().getTokenValue());
                    tokens.put("refresh_token_expires_at", client.getRefreshToken().getExpiresAt());
                }

                if (auth != null && auth.getPrincipal() instanceof OidcUser oidcUser && oidcUser.getIdToken() != null) {
                    tokens.put("id_token", oidcUser.getIdToken().getTokenValue());
                    tokens.put("id_token_expires_at", oidcUser.getIdToken().getExpiresAt());
                }

                model.addAttribute("tokens", tokens);
            }
        }

        return "home"; // render templates/home.html
    }

    private String firstNonEmpty(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
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
