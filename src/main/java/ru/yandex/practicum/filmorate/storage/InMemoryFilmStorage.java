package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Реализация FilmStorage, которая хранит фильмы в памяти.
 */
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new ConcurrentHashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    @Override
    public Film create(Film film) {
        film.setId(nextId.getAndIncrement());
        Film savedFilm = copyFilm(film);
        films.put(savedFilm.getId(), savedFilm);
        log.info("Добавлен фильм: id={}, name={}", film.getId(), film.getName());
        return copyFilm(film);
    }

    @Override
    public Film update(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
        }


        Film savedFilm = copyFilm(film);
        films.put(savedFilm.getId(), savedFilm);

        log.info("Обновлен фильм: id={}, name={}", savedFilm.getId(), savedFilm.getName());
        return copyFilm(savedFilm);
    }

    @Override
    public void delete(int id) {
        findById(id);
        films.remove(id);
        log.info("Удален фильм: id={}", id);
    }

    @Override
    public Film findById(int id) {
        Film film = films.get(id);
        if (film == null) {
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        return copyFilm(film);
    }

    @Override
    public Collection<Film> findAll() {
        ArrayList<Film> filmCopies = new ArrayList<>();

        for (Film film : films.values()) {
            filmCopies.add(copyFilm(film));
        }

        // Сортируем фильмы по ID перед возвратом
        filmCopies.sort(Comparator.comparingInt(Film::getId));
        return filmCopies;
    }

    @Override
    public void addLike(int filmId, int userId) {
        Film film = findById(filmId);
        film.getLikes().add(userId);
        update(film);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        Film film = findById(filmId);
        film.getLikes().remove(userId);
        update(film);
    }

    @Override
    public Collection<Film> findPopular(int count) {
        return films.values().stream()
                .map(this::copyFilm)
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size())
                        .reversed()
                        .thenComparingInt(Film::getId))
                .limit(count)
                .toList();
    }

    // Создаем копию объекта Film, чтобы избежать изменения оригинального объекта при обновлении
    private Film copyFilm(Film film) {
        Film copy = new Film();
        copy.setId(film.getId());
        copy.setName(film.getName());
        copy.setDescription(film.getDescription());
        copy.setReleaseDate(film.getReleaseDate());
        copy.setDuration(film.getDuration());
        copy.setLikes(new HashSet<>(film.getLikes()));
        if (film.getMpa() != null) {
            Mpa mpa = new Mpa();
            mpa.setId(film.getMpa().getId());
            mpa.setName(film.getMpa().getName());
            copy.setMpa(mpa);
        }
        HashSet<Genre> genres = new HashSet<>();
        for (Genre genre : film.getGenres()) {
            Genre genreCopy = new Genre();
            genreCopy.setId(genre.getId());
            genreCopy.setName(genre.getName());
            genres.add(genreCopy);
        }
        copy.setGenres(genres);
        return copy;
    }
}
