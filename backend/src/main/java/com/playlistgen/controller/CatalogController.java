package com.playlistgen.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.playlistgen.model.DecadeOption;
import com.playlistgen.model.GenreOption;
import com.playlistgen.model.RandomFiltersDto;
import com.playlistgen.service.MusicCatalog;

@RestController
public class CatalogController {

    private final MusicCatalog catalog;

    public CatalogController(MusicCatalog catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/api/genres")
    public List<GenreOption> genres() {
        return catalog.genres();
    }

    @GetMapping("/api/decades")
    public List<DecadeOption> decades() {
        return catalog.decades();
    }

    @GetMapping("/api/filters/random")
    public RandomFiltersDto randomFilters() {
        return new RandomFiltersDto(catalog.randomGenre(), catalog.randomDecade());
    }
}
