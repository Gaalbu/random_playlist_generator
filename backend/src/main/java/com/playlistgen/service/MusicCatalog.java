package com.playlistgen.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.playlistgen.model.DecadeOption;
import com.playlistgen.model.GenreOption;

/**
 * The YouTube Data API has no music-genre taxonomy, so genres/decades are curated here and
 * resolved to search keywords instead of relying on category IDs or upload dates.
 */
@Component
public class MusicCatalog {

    private final List<GenreOption> genres = List.of(
        new GenreOption("rock", "Rock", "rock"),
        new GenreOption("pop", "Pop", "pop"),
        new GenreOption("mpb", "MPB", "mpb"),
        new GenreOption("sertanejo", "Sertanejo", "sertanejo"),
        new GenreOption("eletronica", "Eletrônica", "eletronica edm"),
        new GenreOption("hiphop", "Hip Hop / Rap", "hip hop rap"),
        new GenreOption("jazz", "Jazz", "jazz"),
        new GenreOption("classica", "Clássica", "musica classica"),
        new GenreOption("funk", "Funk", "funk brasileiro"),
        new GenreOption("indie", "Indie", "indie rock"),
        new GenreOption("metal", "Metal", "heavy metal"),
        new GenreOption("reggae", "Reggae", "reggae"),
        new GenreOption("samba", "Samba / Pagode", "samba pagode"),
        new GenreOption("rnb", "R&B / Soul", "rnb soul")
    );

    private final List<DecadeOption> decades = List.of(
        new DecadeOption("60s", "Anos 60", "anos 60"),
        new DecadeOption("70s", "Anos 70", "anos 70"),
        new DecadeOption("80s", "Anos 80", "anos 80"),
        new DecadeOption("90s", "Anos 90", "anos 90"),
        new DecadeOption("2000s", "Anos 2000", "anos 2000"),
        new DecadeOption("2010s", "Anos 2010", "anos 2010"),
        new DecadeOption("2020s", "Atual", "2024 2025")
    );

    public List<GenreOption> genres() {
        return genres;
    }

    public List<DecadeOption> decades() {
        return decades;
    }

    public Optional<GenreOption> findGenre(String id) {
        return genres.stream().filter(g -> g.id().equals(id)).findFirst();
    }

    public Optional<DecadeOption> findDecade(String id) {
        return decades.stream().filter(d -> d.id().equals(id)).findFirst();
    }

    public GenreOption randomGenre() {
        return genres.get((int) (Math.random() * genres.size()));
    }

    public DecadeOption randomDecade() {
        return decades.get((int) (Math.random() * decades.size()));
    }
}
