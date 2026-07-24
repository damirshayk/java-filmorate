package ru.yandex.practicum.filmorate.model;

import lombok.Data;

/**
 * Возрастной рейтинг Ассоциации кинокомпаний.
 */
@Data
public class Mpa {
    // Уникальный номер рейтинга.
    private int id;

    // Название рейтинга.
    private String name;
}
