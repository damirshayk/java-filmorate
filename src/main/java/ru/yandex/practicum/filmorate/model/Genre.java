package ru.yandex.practicum.filmorate.model;

import lombok.Data;

/**
 * Жанр фильма.
 */
@Data
public class Genre {
    // Уникальный номер жанра.
    private int id;

    // Название жанра.
    private String name;
}
