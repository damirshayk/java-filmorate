package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Реализация интерфейса FilmStorage для работы с базой данных.
 */
@Slf4j
@Component("filmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private static final String SELECT_FILM = """
            SELECT f.film_id,
                   f.name AS film_name,
                   f.description,
                   f.release_date,
                   f.duration,
                   m.mpa_id,
                   m.name AS mpa_name
            FROM films f
            JOIN mpa_ratings m ON m.mpa_id = f.mpa_id
            """;
    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmMapper;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;
    private final UserStorage userStorage;

    public FilmDbStorage(
            JdbcTemplate jdbc,
            FilmRowMapper filmMapper,
            GenreStorage genreStorage,
            MpaStorage mpaStorage,
            @Qualifier("userDbStorage") UserStorage userStorage
    ) {
        this.jdbc = jdbc;
        this.filmMapper = filmMapper;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
        this.userStorage = userStorage;
    }

    @Override
    @Transactional
    public Film create(Film film) {
        prepareReferences(film);
        String sql = """
                INSERT INTO films(name, description, release_date, duration, mpa_id)
                VALUES (?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, film.getName());
            statement.setString(2, film.getDescription());
            statement.setObject(3, film.getReleaseDate());
            statement.setInt(4, film.getDuration());
            statement.setInt(5, film.getMpa().getId());
            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Не удалось получить идентификатор созданного фильма");
        }
        film.setId(key.intValue());
        saveGenres(film);
        log.info("Добавлен фильм в БД: id={}, name={}", film.getId(), film.getName());
        return findById(film.getId());
    }

    @Override
    @Transactional
    public Film update(Film film) {
        findById(film.getId());
        prepareReferences(film);
        int updated = jdbc.update("""
                        UPDATE films
                        SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ?
                        WHERE film_id = ?
                        """,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        if (updated == 0) {
            throw filmNotFound(film.getId());
        }

        jdbc.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        saveGenres(film);
        log.info("Обновлен фильм в БД: id={}, name={}", film.getId(), film.getName());
        return findById(film.getId());
    }

    @Override
    @Transactional
    public void delete(int id) {
        findById(id);
        jdbc.update("DELETE FROM films WHERE film_id = ?", id);
        log.info("Удален фильм из БД: id={}", id);
    }

    @Override
    public Film findById(int id) {
        List<Film> films = jdbc.query(
                SELECT_FILM + " WHERE f.film_id = ?",
                filmMapper,
                id);
        if (films.isEmpty()) {
            throw filmNotFound(id);
        }
        loadRelations(films);
        return films.getFirst();
    }

    @Override
    public Collection<Film> findAll() {
        List<Film> films = jdbc.query(
                SELECT_FILM + " ORDER BY f.film_id",
                filmMapper);
        loadRelations(films);
        return films;
    }

    @Override
    @Transactional
    public void addLike(int filmId, int userId) {
        findById(filmId);
        userStorage.findById(userId);
        jdbc.update("""
                MERGE INTO likes(film_id, user_id)
                KEY(film_id, user_id)
                VALUES (?, ?)
                """, filmId, userId);
    }

    @Override
    @Transactional
    public void removeLike(int filmId, int userId) {
        findById(filmId);
        userStorage.findById(userId);
        jdbc.update("""
                DELETE FROM likes
                WHERE film_id = ? AND user_id = ?
                """, filmId, userId);
    }

    @Override
    public Collection<Film> findPopular(int count) {
        List<Film> films = jdbc.query("""
                        SELECT f.film_id,
                               f.name AS film_name,
                               f.description,
                               f.release_date,
                               f.duration,
                               m.mpa_id,
                               m.name AS mpa_name,
                               COUNT(l.user_id) AS likes_count
                        FROM films f
                        JOIN mpa_ratings m ON m.mpa_id = f.mpa_id
                        LEFT JOIN likes l ON l.film_id = f.film_id
                        GROUP BY f.film_id,
                                 f.name,
                                 f.description,
                                 f.release_date,
                                 f.duration,
                                 m.mpa_id,
                                 m.name
                        ORDER BY likes_count DESC, f.film_id
                        LIMIT ?
                        """,
                filmMapper,
                count);
        loadRelations(films);
        return films;
    }

    // Подготавливает ссылки на объекты MPA и жанров для фильма перед сохранением или обновлением.
    private void prepareReferences(Film film) {
        int mpaId = film.getMpa() == null ? 1 : film.getMpa().getId();
        Mpa mpa = mpaStorage.findById(mpaId);
        film.setMpa(mpa);

        Set<Genre> inputGenres = film.getGenres() == null ? Set.of() : film.getGenres();
        LinkedHashSet<Genre> genres = new LinkedHashSet<>();
        inputGenres.stream()
                .map(Genre::getId)
                .distinct()
                .sorted()
                .map(genreStorage::findById)
                .forEach(genres::add);
        film.setGenres(genres);
    }

    // Сохраняет жанры фильма в таблице film_genres.
    private void saveGenres(Film film) {
        for (Genre genre : film.getGenres()) {
            jdbc.update("""
                    MERGE INTO film_genres(film_id, genre_id)
                    KEY(film_id, genre_id)
                    VALUES (?, ?)
                    """, film.getId(), genre.getId());
        }
    }


    // Загружает жанры и лайки для списка фильмов двумя запросами.
    private void loadRelations(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }

        Map<Integer, Film> filmsById = new LinkedHashMap<>();
        for (Film film : films) {
            film.setGenres(new LinkedHashSet<>());
            film.setLikes(new LinkedHashSet<>());
            filmsById.put(film.getId(), film);
        }

        String placeholders = String.join(", ", Collections.nCopies(films.size(), "?"));
        Object[] filmIds = filmsById.keySet().toArray();

        jdbc.query("""
                        SELECT fg.film_id, g.genre_id, g.name
                        FROM film_genres fg
                        JOIN genres g ON g.genre_id = fg.genre_id
                        WHERE fg.film_id IN (%s)
                        ORDER BY fg.film_id, g.genre_id
                        """.formatted(placeholders),
                resultSet -> {
                    Film film = filmsById.get(resultSet.getInt("film_id"));
                    Genre genre = new Genre();
                    genre.setId(resultSet.getInt("genre_id"));
                    genre.setName(resultSet.getString("name"));
                    film.getGenres().add(genre);
                },
                filmIds);

        jdbc.query("""
                        SELECT film_id, user_id
                FROM likes
                        WHERE film_id IN (%s)
                        ORDER BY film_id, user_id
                        """.formatted(placeholders),
                resultSet -> {
                    filmsById.get(resultSet.getInt("film_id"))
                            .getLikes()
                            .add(resultSet.getInt("user_id"));
                },
                filmIds);
    }

    // Возвращает исключение NotFoundException с сообщением о том, что фильм с указанным идентификатором не найден.
    private NotFoundException filmNotFound(int id) {
        return new NotFoundException("Фильм с id = " + id + " не найден");
    }
}
