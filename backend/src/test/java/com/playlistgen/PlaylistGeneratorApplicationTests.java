package com.playlistgen;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

// Dummy OAuth2 client credentials: only needed so the context can wire up the
// registration bean in CI/local test runs, no real Google project is contacted.
@SpringBootTest
@TestPropertySource(properties = {
	"spring.security.oauth2.client.registration.google.client-id=test-client-id",
	"spring.security.oauth2.client.registration.google.client-secret=test-client-secret"
})
class PlaylistGeneratorApplicationTests {

	@Test
	void contextLoads() {
	}

}
