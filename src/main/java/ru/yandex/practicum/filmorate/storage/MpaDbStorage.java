package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mapper.MpaRowMapper;

import java.util.Collection;
import java.util.List;

/**
 * Класс MpaDbStorage реализует интерфейс MpaStorage для работы с рейтингами MPA в базе данных.
 */
@Component
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbc;
    private final MpaRowMapper mapper;

    /**
     * Находит рейтинг MPA по его ID.
     *
     * @param id ID рейтинга
     * @return рейтинг MPA
     * @throws NotFoundException если рейтинг с указанным ID не найден
     */
    @Override
    public Mpa findById(int id) {
        List<Mpa> ratings = jdbc.query("""
                SELECT mpa_id, name
                FROM mpa_ratings
                WHERE mpa_id = ?
                """, mapper, id);
        if (ratings.isEmpty()) {
            throw new NotFoundException("Рейтинг MPA с id = " + id + " не найден");
        }
        return ratings.getFirst();
    }

    /**
     * Возвращает все рейтинги MPA.
     *
     * @return список рейтингов
     */
    @Override
    public Collection<Mpa> findAll() {
        return jdbc.query("""
                SELECT mpa_id, name
                FROM mpa_ratings
                ORDER BY mpa_id
                """, mapper);
    }
}
