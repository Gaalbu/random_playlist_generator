package com.playlistgen;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// No Google OAuth properties set here on purpose: the context must still load with just
// YOUTUBE_API_KEY, since GOOGLE_CLIENT_ID is optional (see GoogleOAuthEnvironmentPostProcessor).
@SpringBootTest
class PlaylistGeneratorApplicationNoOAuthTests {

    @Test
    void contextLoadsWithoutGoogleOAuthCredentials() {
    }

}
