package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;

/**
 * Сервис для работы с жанрами.
 */
@Service
public class GenreService {
    private final GenreStorage genreStorage;

    public GenreService(GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    /**
     * Возвращает жанр по его ID.
     *
     * @param id ID жанра
     * @return жанр
     */
    public Genre findById(int id) {
        return genreStorage.findById(id);
    }

    /**
     * Возвращает все жанры.
     *
     * @return список жанров
     */
    public Collection<Genre> findAll() {
        return genreStorage.findAll();
    }
}
