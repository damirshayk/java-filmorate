package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;

import java.util.Collection;
import java.util.List;

/**
 * Реализация интерфейса GenreStorage для работы с базой данных.
 */
@Component
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbc;
    private final GenreRowMapper mapper;

    /**
     * Находит жанр по его ID.
     *
     * @param id ID жанра
     * @return найденный жанр
     * @throws NotFoundException если жанр с указанным ID не найден
     */
    @Override
    public Genre findById(int id) {
        List<Genre> genres = jdbc.query("""
                SELECT genre_id, name
                FROM genres
                WHERE genre_id = ?
                """, mapper, id);
        if (genres.isEmpty()) {
            throw new NotFoundException("Жанр с id = " + id + " не найден");
        }
        return genres.getFirst();
    }

    /**
     * Возвращает все жанры.
     *
     * @return коллекция всех жанров
     */
    @Override
    public Collection<Genre> findAll() {
        return jdbc.query("""
                SELECT genre_id, name
                FROM genres
                ORDER BY genre_id
                """, mapper);
    }
}
