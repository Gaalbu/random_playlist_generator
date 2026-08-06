package com.playlistgen.model;

import java.util.List;

public record GenerateRequest(List<String> genreIds, List<String> decadeIds, Integer trackCount) {
}
