package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * Реализация FilmStorage, которая хранит фильмы в памяти.
 */
@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    // Хранение фильмов в ConcurrentHashMap для потокобезопасности
    private final Map<Integer, Film> films = new ConcurrentHashMap<>();
    // AtomicInteger для генерации уникальных идентификаторов фильмов
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

    // Создаем копию объекта Film, чтобы избежать изменения оригинального объекта при обновлении
    private Film copyFilm(Film film) {
        Film copy = new Film();
        copy.setId(film.getId());
        copy.setName(film.getName());
        copy.setDescription(film.getDescription());
        copy.setReleaseDate(film.getReleaseDate());
        copy.setDuration(film.getDuration());
        copy.setLikes(new HashSet<>(film.getLikes()));
        return copy;
    }
}
