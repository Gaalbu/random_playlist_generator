package com.playlistgen.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.google.api.services.youtube.model.PlaylistStatus;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.playlistgen.model.TrackDto;

/**
 * Read-only search runs against a plain API key (public data, no user context needed).
 * Playlist creation/insertion needs the logged-in user's OAuth2 access token instead.
 */
@Service
public class YouTubeService {

    private static final String APPLICATION_NAME = "playlist-generator";

    private final String apiKey;
    private final ObjectProvider<OAuth2AuthorizedClientService> authorizedClientService;
    private final HttpTransport transport;
    private final GsonFactory jsonFactory;

    public YouTubeService(
        @Value("${app.youtube.api-key}") String apiKey,
        ObjectProvider<OAuth2AuthorizedClientService> authorizedClientService
    ) throws GeneralSecurityException, IOException {
        this.apiKey = apiKey;
        this.authorizedClientService = authorizedClientService;
        this.transport = GoogleNetHttpTransport.newTrustedTransport();
        this.jsonFactory = GsonFactory.getDefaultInstance();
    }

    public List<TrackDto> search(String query, long maxResults) throws IOException {
        YouTube.Search.List request = publicClient().search()
            .list(List.of("snippet"))
            .setQ(query)
            .setType(List.of("video"))
            .setVideoCategoryId("10")
            .setMaxResults(maxResults)
            .setSafeSearch("none");

        SearchListResponse response = request.execute();
        if (response.getItems() == null) {
            return List.of();
        }
        return response.getItems().stream()
            .map(item -> new TrackDto(
                item.getId().getVideoId(),
                item.getSnippet().getTitle(),
                item.getSnippet().getChannelTitle(),
                thumbnailUrl(item.getSnippet())
            ))
            .toList();
    }

    public String createPlaylist(OAuth2AuthenticationToken authentication, String title) throws IOException {
        PlaylistSnippet snippet = new PlaylistSnippet()
            .setTitle(title)
            .setDescription("Gerado automaticamente pelo Playlist Generator");
        PlaylistStatus status = new PlaylistStatus().setPrivacyStatus("private");
        Playlist playlist = new Playlist().setSnippet(snippet).setStatus(status);

        Playlist created = authenticatedClient(authentication)
            .playlists()
            .insert(List.of("snippet", "status"), playlist)
            .execute();
        return created.getId();
    }

    public void addToPlaylist(OAuth2AuthenticationToken authentication, String playlistId, List<String> videoIds) throws IOException {
        YouTube youtube = authenticatedClient(authentication);
        for (String videoId : videoIds) {
            ResourceId resourceId = new ResourceId()
                .setKind("youtube#video")
                .setVideoId(videoId);
            PlaylistItemSnippet snippet = new PlaylistItemSnippet()
                .setPlaylistId(playlistId)
                .setResourceId(resourceId);
            PlaylistItem item = new PlaylistItem().setSnippet(snippet);
            youtube.playlistItems().insert(List.of("snippet"), item).execute();
        }
    }

    private String thumbnailUrl(com.google.api.services.youtube.model.SearchResultSnippet snippet) {
        var thumbnails = snippet.getThumbnails();
        if (thumbnails.getHigh() != null) {
            return thumbnails.getHigh().getUrl();
        }
        if (thumbnails.getMedium() != null) {
            return thumbnails.getMedium().getUrl();
        }
        return thumbnails.getDefault().getUrl();
    }

    private YouTube publicClient() {
        return new YouTube.Builder(transport, jsonFactory, request -> { })
            .setApplicationName(APPLICATION_NAME)
            .setYouTubeRequestInitializer(new YouTubeRequestInitializer(apiKey))
            .build();
    }

    private YouTube authenticatedClient(OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client = authorizedClientService.getObject().loadAuthorizedClient(
            authentication.getAuthorizedClientRegistrationId(),
            authentication.getName()
        );
        String accessToken = client.getAccessToken().getTokenValue();
        HttpRequestInitializer initializer = request -> request.getHeaders().setAuthorization("Bearer " + accessToken);
        return new YouTube.Builder(transport, jsonFactory, initializer)
            .setApplicationName(APPLICATION_NAME)
            .build();
    }
}
