package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

/*
 * Интерфейс для хранения фильмов.
 */
public interface FilmStorage {
    Film create(Film film);

    Film update(Film film);

    void delete(int id);

    Film findById(int id);

    Collection<Film> findAll();

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);

    Collection<Film> findPopular(int count);
}
