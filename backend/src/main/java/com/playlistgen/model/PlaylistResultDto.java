package com.playlistgen.model;

import java.util.List;

public record PlaylistResultDto(
    String title,
    List<GenreOption> genres,
    List<DecadeOption> decades,
    List<TrackDto> tracks
) {
}
