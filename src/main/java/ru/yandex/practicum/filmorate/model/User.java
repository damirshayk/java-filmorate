package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;

/**
 * Пользователь приложения.
 */
@Data
public class User {
    // Уникальный номер пользователя.
    private int id;

    // Электронная почта пользователя.
    private String email;

    // Логин пользователя.
    private String login;

    // Имя, которое будет видно другим пользователям.
    private String name;

    // Дата рождения пользователя.
    private LocalDate birthday;
}
