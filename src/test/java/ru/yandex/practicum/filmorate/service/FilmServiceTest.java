package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilmServiceTest {
    private FilmService service;
    private UserStorage userStorage;

    @BeforeEach
    void setUp() {
        FilmStorage filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        service = new FilmService(filmStorage, userStorage);
    }

    @Test
    void shouldAddLikeOnceAndRemoveIt() {
        Film film = service.create(makeFilm("Film"));
        User user = userStorage.create(makeUser("user"));

        service.addLike(film.getId(), user.getId());
        service.addLike(film.getId(), user.getId());

        assertEquals(1, service.findById(film.getId()).getLikes().size());
        service.removeLike(film.getId(), user.getId());
        assertTrue(service.findById(film.getId()).getLikes().isEmpty());
    }

    @Test
    void shouldReturnPopularFilmsByLikesAndLimit() {
        Film noLikes = service.create(makeFilm("No likes"));
        Film oneLike = service.create(makeFilm("One like"));
        Film twoLikes = service.create(makeFilm("Two likes"));
        User first = userStorage.create(makeUser("first"));
        User second = userStorage.create(makeUser("second"));
        service.addLike(oneLike.getId(), first.getId());
        service.addLike(twoLikes.getId(), first.getId());
        service.addLike(twoLikes.getId(), second.getId());

        List<Film> popular = service.getPopularFilms(2);

        assertEquals(List.of(twoLikes.getId(), oneLike.getId()),
                popular.stream().map(Film::getId).toList());
        assertTrue(popular.stream().noneMatch(film -> film.getId() == noLikes.getId()));
    }

    @Test
    void shouldRejectNonPositivePopularCount() {
        assertThrows(ValidationException.class, () -> service.getPopularFilms(0));
        assertThrows(ValidationException.class, () -> service.getPopularFilms(-1));
    }

    @Test
    void shouldThrowWhenFilmOrUserDoesNotExist() {
        Film film = service.create(makeFilm("Film"));
        User user = userStorage.create(makeUser("user"));

        assertThrows(NotFoundException.class, () -> service.addLike(99, user.getId()));
        assertThrows(NotFoundException.class, () -> service.addLike(film.getId(), 99));
        assertThrows(NotFoundException.class, () -> service.removeLike(99, user.getId()));
    }

    private Film makeFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);
        return film;
    }

    private User makeUser(String login) {
        User user = new User();
        user.setEmail(login + "@example.com");
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }
}
