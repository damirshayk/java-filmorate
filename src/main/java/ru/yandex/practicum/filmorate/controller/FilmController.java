package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

/**
 * Контроллер для работы с фильмами.
 */
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    /**
     * Создает новый фильм.
     * @param film фильм для создания
     * @return созданный фильм
     */
    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        return filmService.create(film);
    }

    /**
     * Обновляет информацию о фильме.
     * @param film фильм для обновления
     * @return обновленный фильм
     */
    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        return filmService.update(film);
    }

    /**
     * Возвращает список всех фильмов.
     * @return список фильмов
     */
    @GetMapping
    public Collection<Film> findAll() {
        return filmService.findAll();
    }

    /**
     * Возвращает фильм по его ID.
     * @param id ID фильма
     * @return фильм
     */
    @GetMapping("/{id}")
    public Film findById(@PathVariable int id) {
        return filmService.findById(id);
    }

    /**
     * Добавляет лайк фильму.
     * @param id ID фильма
     * @param userId ID пользователя
     */
    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        filmService.addLike(id, userId);
    }

    /**
     * Удаляет лайк у фильма.
     * @param id ID фильма
     * @param userId ID пользователя
     */
    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        filmService.removeLike(id, userId);
    }

    /**
     * Возвращает список популярных фильмов.
     * @param count количество фильмов для возврата (по умолчанию 10)
     * @return список популярных фильмов
     */
    @GetMapping("/popular")
    public Collection<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        return filmService.getPopularFilms(count);
    }
}
