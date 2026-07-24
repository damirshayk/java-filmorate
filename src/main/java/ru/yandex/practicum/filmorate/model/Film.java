package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Фильм, который хранится в приложении.
 */
@Data
public class Film {
    // Уникальный номер фильма.
    private int id;

    // Название фильма.
    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    // Короткое описание фильма.
    @Size(max = 200, message = "Описание фильма не может быть длиннее 200 символов")
    private String description;

    // Дата выхода фильма.
    @NotNull(message = "Дата релиза должна быть указана")
    private LocalDate releaseDate;

    // Продолжительность фильма в минутах.
    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private int duration;

    // Возрастной рейтинг фильма.
    private Mpa mpa;

    // Жанры фильма.
    private Set<Genre> genres = new HashSet<>();

    // Идентификаторы пользователей, поставивших лайк.
    private Set<Integer> likes = new HashSet<>();
}
