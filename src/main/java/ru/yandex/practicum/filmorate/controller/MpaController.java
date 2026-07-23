package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.Collection;

/**
 * Контроллер для работы с рейтингами MPA.
 */
@RestController
@RequestMapping("/mpa")
public class MpaController {
    private final MpaService mpaService;

    public MpaController(MpaService mpaService) {
        this.mpaService = mpaService;
    }

    /**
     * Возвращает все рейтинги MPA.
     *
     * @return список рейтингов
     */
    @GetMapping
    public Collection<Mpa> findAll() {
        return mpaService.findAll();
    }

    /**
     * Возвращает рейтинг MPA по его ID.
     *
     * @param id ID рейтинга
     * @return рейтинг MPA
     */
    @GetMapping("/{id}")
    public Mpa findById(@PathVariable int id) {
        return mpaService.findById(id);
    }
}
