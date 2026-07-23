package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.Collection;

/**
 * Контроллер для работы с жанрами.
 */
@RestController
@RequestMapping("/genres")
public class GenreController {
    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    /**
     * Возвращает все жанры.
     *
     * @return список жанров
     */
    @GetMapping
    public Collection<Genre> findAll() {
        return genreService.findAll();
    }

    /**
     * Возвращает жанр по его ID.
     *
     * @param id ID жанра
     * @return жанр
     */
    @GetMapping("/{id}")
    public Genre findById(@PathVariable int id) {
        return genreService.findById(id);
    }
}
