package com.playlistgen.controller;

import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final ObjectProvider<ClientRegistrationRepository> clientRegistrations;

    public AuthController(ObjectProvider<ClientRegistrationRepository> clientRegistrations) {
        this.clientRegistrations = clientRegistrations;
    }

    @GetMapping("/api/auth/status")
    public Map<String, Object> status(Authentication authentication) {
        boolean authenticated = authentication instanceof OAuth2AuthenticationToken && authentication.isAuthenticated();
        return Map.of(
            "authenticated", authenticated,
            "oauthEnabled", clientRegistrations.getIfAvailable() != null,
            "loginUrl", "/oauth2/authorization/google"
        );
    }
}
