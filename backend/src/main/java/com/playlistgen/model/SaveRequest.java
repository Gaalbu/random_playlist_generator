package com.playlistgen.model;

import java.util.List;

public record SaveRequest(String title, List<String> videoIds) {
}
