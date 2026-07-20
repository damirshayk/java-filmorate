package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Тесты проверки фильмов.
 */
class FilmControllerTest {
    private FilmController controller;

    @BeforeEach
    void setUp() {
        controller = new FilmController(new FilmService(new InMemoryFilmStorage(), new InMemoryUserStorage()));
    }

    @Test
    @DisplayName("Фильм с правильными данными создается")
    void shouldCreateFilmWithValidData() {
        Film film = makeValidFilm();

        Film created = controller.create(film);

        assertEquals(1, created.getId());
        assertEquals("Film", created.getName());
    }

    @Test
    @DisplayName("Пустой запрос фильма не проходит проверку")
    void shouldThrowExceptionWhenFilmRequestIsEmpty() {
        Film film = new Film();

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    @DisplayName("Фильм без названия не проходит проверку")
    void shouldThrowExceptionWhenFilmNameIsBlank() {
        Film film = makeValidFilm();
        film.setName("   ");

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    @DisplayName("Описание длиной 200 символов проходит проверку")
    void shouldCreateFilmWhenDescriptionLengthIs200() {
        Film film = makeValidFilm();
        film.setDescription("a".repeat(200));

        assertDoesNotThrow(() -> controller.create(film));
    }

    @Test
    @DisplayName("Описание длиннее 200 символов не проходит проверку")
    void shouldThrowExceptionWhenDescriptionLengthIsMoreThan200() {
        Film film = makeValidFilm();
        film.setDescription("a".repeat(201));

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    @DisplayName("Дата релиза 28 декабря 1895 года проходит проверку")
    void shouldCreateFilmWhenReleaseDateIsCinemaBirthday() {
        Film film = makeValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 28));

        assertDoesNotThrow(() -> controller.create(film));
    }

    @Test
    @DisplayName("Дата релиза раньше 28 декабря 1895 года не проходит проверку")
    void shouldThrowExceptionWhenReleaseDateIsBeforeCinemaBirthday() {
        Film film = makeValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    @DisplayName("Положительная продолжительность проходит проверку")
    void shouldCreateFilmWhenDurationIsPositive() {
        Film film = makeValidFilm();
        film.setDuration(1);

        assertDoesNotThrow(() -> controller.create(film));
    }

    @Test
    @DisplayName("Нулевая продолжительность не проходит проверку")
    void shouldThrowExceptionWhenDurationIsZero() {
        Film film = makeValidFilm();
        film.setDuration(0);

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    @DisplayName("Отрицательная продолжительность не проходит проверку")
    void shouldThrowExceptionWhenDurationIsNegative() {
        Film film = makeValidFilm();
        film.setDuration(-1);

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    /**
     * Создает фильм с правильными данными для тестов.
     */
    private Film makeValidFilm() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);
        return film;
    }
}
