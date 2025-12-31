package com.skrudra.sso.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final ClientRegistrationRepository clientRegistrationRepository;

    public SecurityConfig(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/home").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(Customizer.withDefaults())
            .oauth2ResourceServer(rs -> rs.jwt(Customizer.withDefaults()))
            .logout(logout -> logout
                .logoutSuccessHandler((request, response, authentication) -> {
                    String logoutUrl = "http://localhost:9090/realms/Demo-Realm/protocol/openid-connect/logout";
                    String clientId = clientRegistrationRepository.findByRegistrationId("keycloak-oidc").getClientId();
                    String redirectUri = "http://localhost:8081/home";
                    String logoutRedirectUrl = String.format("%s?client_id=%s&post_logout_redirect_uri=%s",
                            logoutUrl, clientId, redirectUri);
                    response.sendRedirect(logoutRedirectUrl);
                })
            );

        return http.build();
    }
}
