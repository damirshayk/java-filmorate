package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;

/**
 * Фильм, который хранится в приложении.
 */
@Data
public class Film {
    // Уникальный номер фильма.
    private int id;

    // Название фильма.
    private String name;

    // Короткое описание фильма.
    private String description;

    // Дата выхода фильма.
    private LocalDate releaseDate;

    // Продолжительность фильма в минутах.
    private int duration;
}
