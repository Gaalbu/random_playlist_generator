package com.playlistgen.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @GetMapping("/api/auth/status")
    public Map<String, Object> status(Authentication authentication) {
        boolean authenticated = authentication instanceof OAuth2AuthenticationToken && authentication.isAuthenticated();
        return Map.of(
            "authenticated", authenticated,
            "loginUrl", "/oauth2/authorization/google"
        );
    }
}
