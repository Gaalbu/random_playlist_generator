package com.playlistgen.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import com.playlistgen.model.DecadeOption;
import com.playlistgen.model.GenreOption;
import com.playlistgen.model.TrackDto;

class PlaylistGeneratorServiceTest {

    private final PlaylistGeneratorService service =
        new PlaylistGeneratorService(null, null, new Random(42));

    @Test
    void buildsQueryFromGenreAndDecadeKeywords() {
        List<GenreOption> genres = List.of(new GenreOption("rock", "Rock", "rock"));
        List<DecadeOption> decades = List.of(new DecadeOption("80s", "Anos 80", "anos 80"));

        String query = service.buildQuery(genres, decades);

        assertThat(query).isEqualTo("rock anos 80 music");
    }

    @Test
    void combinesMultipleGenresAndDecadesIntoOneQuery() {
        List<GenreOption> genres = List.of(
            new GenreOption("rock", "Rock", "rock"),
            new GenreOption("indie", "Indie", "indie rock")
        );
        List<DecadeOption> decades = List.of(new DecadeOption("90s", "Anos 90", "anos 90"));

        String query = service.buildQuery(genres, decades);

        assertThat(query).isEqualTo("rock indie rock anos 90 music");
    }

    @Test
    void dedupesTracksByVideoIdKeepingFirstOccurrence() {
        List<TrackDto> tracks = List.of(
            new TrackDto("abc", "Song A", "Channel A", "thumb-a"),
            new TrackDto("abc", "Song A duplicate", "Channel A", "thumb-a-dup"),
            new TrackDto("def", "Song B", "Channel B", "thumb-b")
        );

        List<TrackDto> result = service.dedupeAndShuffle(tracks, 10);

        assertThat(result).extracting(TrackDto::videoId).containsExactlyInAnyOrder("abc", "def");
        assertThat(result).filteredOn(t -> t.videoId().equals("abc"))
            .singleElement()
            .extracting(TrackDto::title)
            .isEqualTo("Song A");
    }

    @Test
    void limitsShuffledResultToRequestedCount() {
        List<TrackDto> tracks = List.of(
            new TrackDto("1", "One", "C", "t"),
            new TrackDto("2", "Two", "C", "t"),
            new TrackDto("3", "Three", "C", "t"),
            new TrackDto("4", "Four", "C", "t")
        );

        List<TrackDto> result = service.dedupeAndShuffle(tracks, 2);

        assertThat(result).hasSize(2);
        assertThat(tracks).containsAll(result);
    }

    @Test
    void clampsTrackCountWithinBounds() {
        assertThat(service.clampTrackCount(3)).isEqualTo(PlaylistGeneratorService.MIN_TRACK_COUNT);
        assertThat(service.clampTrackCount(500)).isEqualTo(PlaylistGeneratorService.MAX_TRACK_COUNT);
        assertThat(service.clampTrackCount(20)).isEqualTo(20);
    }

    @Test
    void picksRandomTrackCountWithinDefaultSpreadWhenNotSpecified() {
        int count = service.clampTrackCount(null);

        assertThat(count).isBetween(15, 25);
    }
}
