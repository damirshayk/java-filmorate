package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;

import java.time.LocalDate;
import java.util.LinkedHashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import({
        FilmDbStorage.class,
        FilmRowMapper.class,
        GenreDbStorage.class,
        GenreRowMapper.class,
        MpaDbStorage.class,
        MpaRowMapper.class,
        UserDbStorage.class,
        UserRowMapper.class
})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    @Test
    void shouldCreateFindAndListFilmWithMpaAndGenres() {
        Film created = filmStorage.create(makeFilm("Film", 3, 3, 5));

        assertThat(created.getId()).isPositive();
        assertThat(created.getMpa().getName()).isEqualTo("PG-13");
        assertThat(created.getGenres())
                .extracting(Genre::getId)
                .containsExactly(3, 5);
        assertThat(filmStorage.findById(created.getId()))
                .usingRecursiveComparison()
                .isEqualTo(created);
        assertThat(filmStorage.findAll())
                .extracting(Film::getId)
                .containsExactly(created.getId());
    }

    @Test
    void shouldUpdateGenresAndDeleteFilm() {
        Film created = filmStorage.create(makeFilm("Film", 3, 3, 5));
        created.setName("Новое название");
        created.setGenres(genres(1, 2));

        Film updated = filmStorage.update(created);

        assertThat(updated.getName()).isEqualTo("Новое название");
        assertThat(updated.getGenres())
                .extracting(Genre::getId)
                .containsExactly(1, 2);

        filmStorage.delete(created.getId());

        assertThatThrownBy(() -> filmStorage.findById(created.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldStoreLikesIdempotentlyAndReturnPopularFilms() {
        User firstUser = userStorage.create(makeUser("first"));
        User secondUser = userStorage.create(makeUser("second"));
        Film firstFilm = filmStorage.create(makeFilm("First", 1));
        Film secondFilm = filmStorage.create(makeFilm("Second", 2));

        filmStorage.addLike(firstFilm.getId(), firstUser.getId());
        filmStorage.addLike(secondFilm.getId(), firstUser.getId());
        filmStorage.addLike(secondFilm.getId(), secondUser.getId());
        filmStorage.addLike(secondFilm.getId(), secondUser.getId());

        assertThat(filmStorage.findPopular(2))
                .extracting(Film::getId)
                .containsExactly(secondFilm.getId(), firstFilm.getId());
        assertThat(filmStorage.findById(secondFilm.getId()).getLikes())
                .containsExactlyInAnyOrder(firstUser.getId(), secondUser.getId());

        filmStorage.removeLike(secondFilm.getId(), secondUser.getId());

        assertThat(filmStorage.findById(secondFilm.getId()).getLikes())
                .containsExactly(firstUser.getId());
    }

    @Test
    void shouldLoadRelationsForSeveralFilmsWithoutMixingThem() {
        User firstUser = userStorage.create(makeUser("first"));
        User secondUser = userStorage.create(makeUser("second"));
        Film firstFilm = filmStorage.create(makeFilm("First", 1, 1, 3));
        Film secondFilm = filmStorage.create(makeFilm("Second", 2, 2, 4));

        filmStorage.addLike(firstFilm.getId(), firstUser.getId());
        filmStorage.addLike(secondFilm.getId(), secondUser.getId());

        assertThat(filmStorage.findAll())
                .satisfiesExactly(
                        film -> {
                            assertThat(film.getGenres())
                                    .extracting(Genre::getId)
                                    .containsExactly(1, 3);
                            assertThat(film.getLikes()).containsExactly(firstUser.getId());
                        },
                        film -> {
                            assertThat(film.getGenres())
                                    .extracting(Genre::getId)
                                    .containsExactly(2, 4);
                            assertThat(film.getLikes()).containsExactly(secondUser.getId());
                        });
    }

    @Test
    void shouldRejectUnknownMpaAndGenre() {
        assertThatThrownBy(() -> filmStorage.create(makeFilm("Unknown MPA", 9999)))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> filmStorage.create(makeFilm("Unknown genre", 1, 9999)))
                .isInstanceOf(NotFoundException.class);
    }

    private Film makeFilm(String name, int mpaId, int... genreIds) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);
        Mpa mpa = new Mpa();
        mpa.setId(mpaId);
        film.setMpa(mpa);
        film.setGenres(genres(genreIds));
        return film;
    }

    private LinkedHashSet<Genre> genres(int... ids) {
        LinkedHashSet<Genre> genres = new LinkedHashSet<>();
        for (int id : ids) {
            Genre genre = new Genre();
            genre.setId(id);
            genres.add(genre);
        }
        return genres;
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
