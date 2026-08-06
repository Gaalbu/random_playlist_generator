package com.playlistgen.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.playlistgen.model.DecadeOption;
import com.playlistgen.model.GenerateRequest;
import com.playlistgen.model.GenreOption;
import com.playlistgen.model.PlaylistResultDto;
import com.playlistgen.model.TrackDto;

@Service
public class PlaylistGeneratorService {

    static final int DEFAULT_TRACK_COUNT = 20;
    static final int MIN_TRACK_COUNT = 5;
    static final int MAX_TRACK_COUNT = 50;
    private static final long SEARCH_RESULTS = 50L;

    private final YouTubeService youTubeService;
    private final MusicCatalog catalog;
    private final Random random;

    @Autowired
    public PlaylistGeneratorService(YouTubeService youTubeService, MusicCatalog catalog) {
        this(youTubeService, catalog, new Random());
    }

    PlaylistGeneratorService(YouTubeService youTubeService, MusicCatalog catalog, Random random) {
        this.youTubeService = youTubeService;
        this.catalog = catalog;
        this.random = random;
    }

    public PlaylistResultDto generate(GenerateRequest request) throws IOException {
        List<GenreOption> genres = resolveGenres(request.genreIds());
        List<DecadeOption> decades = resolveDecades(request.decadeIds());
        int trackCount = clampTrackCount(request.trackCount());

        String query = buildQuery(genres, decades);
        List<TrackDto> searchResults = youTubeService.search(query, SEARCH_RESULTS);
        List<TrackDto> tracks = dedupeAndShuffle(searchResults, trackCount);

        return new PlaylistResultDto(buildTitle(genres, decades), genres, decades, tracks);
    }

    String buildQuery(List<GenreOption> genres, List<DecadeOption> decades) {
        String genreKeywords = genres.stream().map(GenreOption::keyword).collect(Collectors.joining(" "));
        String decadeKeywords = decades.stream().map(DecadeOption::keyword).collect(Collectors.joining(" "));
        return Stream.of(genreKeywords, decadeKeywords, "music")
            .filter(part -> !part.isBlank())
            .collect(Collectors.joining(" "));
    }

    List<TrackDto> dedupeAndShuffle(List<TrackDto> tracks, int count) {
        List<TrackDto> unique = new ArrayList<>(tracks.stream()
            .collect(Collectors.toMap(
                TrackDto::videoId,
                track -> track,
                (first, duplicate) -> first,
                LinkedHashMap::new
            ))
            .values());
        Collections.shuffle(unique, random);
        return unique.stream().limit(count).toList();
    }

    int clampTrackCount(Integer requested) {
        if (requested == null) {
            return DEFAULT_TRACK_COUNT - 5 + random.nextInt(11); // random spread 15..25
        }
        return Math.max(MIN_TRACK_COUNT, Math.min(MAX_TRACK_COUNT, requested));
    }

    private List<GenreOption> resolveGenres(List<String> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return List.of(catalog.randomGenre());
        }
        return genreIds.stream()
            .map(catalog::findGenre)
            .flatMap(java.util.Optional::stream)
            .toList();
    }

    private List<DecadeOption> resolveDecades(List<String> decadeIds) {
        if (decadeIds == null || decadeIds.isEmpty()) {
            return List.of(catalog.randomDecade());
        }
        return decadeIds.stream()
            .map(catalog::findDecade)
            .flatMap(java.util.Optional::stream)
            .toList();
    }

    private String buildTitle(List<GenreOption> genres, List<DecadeOption> decades) {
        String genreLabel = genres.stream().map(GenreOption::label).collect(Collectors.joining(" + "));
        String decadeLabel = decades.stream().map(DecadeOption::label).collect(Collectors.joining(" + "));
        return "%s • %s".formatted(genreLabel, decadeLabel);
    }
}
