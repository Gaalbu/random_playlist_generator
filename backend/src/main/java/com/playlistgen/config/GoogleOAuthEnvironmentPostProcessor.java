package com.playlistgen.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

/**
 * Registers the Google OAuth2 client only when GOOGLE_CLIENT_ID is actually set, so the app boots
 * fine without a Google Cloud project — the "save to YT Music" flow is the only thing that needs
 * it (see SecurityConfig, YouTubeService, AuthController, which all treat it as optional).
 *
 * Done here instead of a profile-gated application-oauth.yml because profile activation from an
 * EnvironmentPostProcessor runs too late to influence which config-data files get loaded.
 */
public class GoogleOAuthEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String clientId = environment.getProperty("GOOGLE_CLIENT_ID");
        if (!StringUtils.hasText(clientId)) {
            return;
        }

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("spring.security.oauth2.client.registration.google.client-id", clientId);
        properties.put("spring.security.oauth2.client.registration.google.client-secret", environment.getProperty("GOOGLE_CLIENT_SECRET"));
        properties.put("spring.security.oauth2.client.registration.google.scope[0]", "openid");
        properties.put("spring.security.oauth2.client.registration.google.scope[1]", "profile");
        properties.put("spring.security.oauth2.client.registration.google.scope[2]", "https://www.googleapis.com/auth/youtube");
        properties.put(
            "spring.security.oauth2.client.provider.google.authorization-uri",
            "https://accounts.google.com/o/oauth2/v2/auth?access_type=offline&prompt=consent"
        );

        environment.getPropertySources().addFirst(new MapPropertySource("googleOAuthRegistration", properties));
    }
}
