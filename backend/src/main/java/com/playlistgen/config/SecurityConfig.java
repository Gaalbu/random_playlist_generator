package com.playlistgen.config;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Read-only endpoints (catalog, filters, search-backed generation) stay open since they only
 * use a server-side API key. Only the "save to YT Music" flow touches the user's account and
 * requires the Google OAuth2 login session — which only exists when GOOGLE_CLIENT_ID is set (see
 * GoogleOAuthEnvironmentPostProcessor). Without it, oauth2Login is skipped entirely instead of
 * failing to boot.
 */
@Configuration
public class SecurityConfig {

    @Value("${app.cors.allowed-origin}")
    private String allowedOrigin;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, ObjectProvider<ClientRegistrationRepository> clientRegistrations) throws Exception {
        boolean oauthEnabled = clientRegistrations.getIfAvailable() != null;

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/genres",
                    "/api/decades",
                    "/api/filters/random",
                    "/api/playlist/generate",
                    "/api/auth/status",
                    "/oauth2/**",
                    "/login/**"
                ).permitAll()
                .anyRequest().authenticated()
            );

        if (oauthEnabled) {
            http.oauth2Login(oauth2 -> oauth2.defaultSuccessUrl(allowedOrigin + "/?connected=1", true));
        }

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigin));
        config.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
