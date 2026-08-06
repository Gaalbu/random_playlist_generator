package com.playlistgen.controller;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.playlistgen.model.GenerateRequest;
import com.playlistgen.model.PlaylistResultDto;
import com.playlistgen.model.SaveRequest;
import com.playlistgen.model.SaveResultDto;
import com.playlistgen.service.PlaylistGeneratorService;
import com.playlistgen.service.YouTubeService;

@RestController
public class PlaylistController {

    private final PlaylistGeneratorService playlistGeneratorService;
    private final YouTubeService youTubeService;

    public PlaylistController(PlaylistGeneratorService playlistGeneratorService, YouTubeService youTubeService) {
        this.playlistGeneratorService = playlistGeneratorService;
        this.youTubeService = youTubeService;
    }

    @PostMapping("/api/playlist/generate")
    public PlaylistResultDto generate(@RequestBody GenerateRequest request) {
        try {
            return playlistGeneratorService.generate(request);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Falha ao buscar músicas no YouTube", e);
        }
    }

    @PostMapping("/api/playlist/save")
    public SaveResultDto save(@RequestBody SaveRequest request, Authentication authentication) {
        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken) || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login com o Google para salvar no YT Music");
        }
        try {
            String playlistId = youTubeService.createPlaylist(oauthToken, request.title());
            youTubeService.addToPlaylist(oauthToken, playlistId, request.videoIds());
            return new SaveResultDto(playlistId, "https://music.youtube.com/playlist?list=" + playlistId);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Falha ao salvar playlist no YouTube Music", e);
        }
    }
}
