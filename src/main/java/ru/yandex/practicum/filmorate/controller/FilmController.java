package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для работы с фильмами.
 */
@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    // Временное хранилище фильмов, пока нет бд.
    private final Map<Integer, Film> films = new HashMap<>();

    private int nextId = 1;

    /**
     * Добавляет новый фильм.
     */
    @PostMapping
    public Film create(@RequestBody Film film) {
        validate(film);
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Добавлен фильм: id={}, name={}", film.getId(), film.getName());
        return film;
    }

    /**
     * Обновляет фильм, который уже есть в хранилище.
     */
    @PutMapping
    public Film update(@RequestBody Film film) {
        if (!films.containsKey(film.getId())) {
            log.warn("Попытка обновления несуществующего фильма: id={}", film.getId());
            throw new IllegalArgumentException("Фильм с id = " + film.getId() + " не найден");
        }

        validate(film);
        films.put(film.getId(), film);
        log.info("Обновлен фильм: id={}, name={}", film.getId(), film.getName());
        return film;
    }

    /**
     * Возвращает все фильмы.
     */
    @GetMapping
    public Collection<Film> findAll() {
        log.info("Запрос на получение всех фильмов, количество={}", films.size());
        return films.values();
    }

    /**
     * Проверяет, что данные фильма подходят под правила задания.
     */
    private void validate(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Попытка создать фильм без названия: id={}", film.getId());
            throw new ValidationException("Название фильма не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.warn("Попытка создать фильм с слишком длинным описанием: id={}", film.getId());
            throw new ValidationException("Описание фильма не может быть длиннее 200 символов");
        }

        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Попытка создать фильм с датой релиза раньше 28 декабря 1895 года: id={}", film.getId());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        if (film.getDuration() <= 0) {
            log.warn("Попытка создать фильм с отрицательной продолжительностью: id={}", film.getId());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}
