package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * Сервис для работы с фильмами.
 */
@Service
public class FilmService {
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    /**
     * Создает новый фильм.
     *
     * @param film фильм для создания
     * @return созданный фильм
     */
    public Film create(Film film) {
        validateReleaseDate(film);
        return filmStorage.create(film);
    }

    /**
     * Обновляет существующий фильм.
     *
     * @param film фильм для обновления
     * @return обновленный фильм
     */
    public Film update(Film film) {
        validateReleaseDate(film);
        return filmStorage.update(film);
    }

    /**
     * Удаляет фильм по его ID.
     *
     * @param id ID фильма
     */
    public void delete(int id) {
        filmStorage.delete(id);
    }

    /**
     * Находит фильм по его ID.
     *
     * @param id ID фильма
     * @return найденный фильм
     */
    public Film findById(int id) {
        return filmStorage.findById(id);
    }

    /**
     * Возвращает все фильмы.
     *
     * @return коллекция всех фильмов
     */
    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    /**
     * Добавляет лайк фильму от пользователя.
     *
     * @param filmId ID фильма
     * @param userId ID пользователя
     */
    public void addLike(int filmId, int userId) {
        userStorage.findById(userId);
        filmStorage.addLike(filmId, userId);
    }

    /**
     * Удаляет лайк фильму от пользователя.
     *
     * @param filmId ID фильма
     * @param userId ID пользователя
     */
    public void removeLike(int filmId, int userId) {
        userStorage.findById(userId);
        filmStorage.removeLike(filmId, userId);
    }

    /**
     * Возвращает список популярных фильмов.
     *
     * @param count количество фильмов для возврата
     * @return список популярных фильмов
     */
    public List<Film> getPopularFilms(int count) {
        if (count <= 0) {
            throw new ValidationException("Количество фильмов должно быть положительным числом");
        }
        return filmStorage.findPopular(count).stream().toList();
    }

    /**
     * Валидирует дату релиза фильма.
     *
     * @param film фильм для валидации
     * @throws ValidationException если дата релиза раньше 28 декабря 1895 года
     */
    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate() != null
                && film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            throw new ValidationException(
                    "Дата релиза не может быть раньше 28 декабря 1895 года"
            );
        }
    }
}
