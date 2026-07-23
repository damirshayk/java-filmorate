package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.Collection;

/**
 * Сервис для работы с рейтингами MPA.
 */
@Service
public class MpaService {
    private final MpaStorage mpaStorage;

    public MpaService(MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    /**
     * Возвращает рейтинг MPA по его ID.
     *
     * @param id ID рейтинга
     * @return рейтинг MPA
     */
    public Mpa findById(int id) {
        return mpaStorage.findById(id);
    }

    /**
     * Возвращает все рейтинги MPA.
     *
     * @return список рейтингов
     */
    public Collection<Mpa> findAll() {
        return mpaStorage.findAll();
    }
}
