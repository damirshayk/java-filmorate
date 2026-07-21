package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Пользователь приложения.
 */
@Data
public class User {
    // Уникальный номер пользователя.
    private int id;

    // Электронная почта пользователя.
    @NotBlank(message = "Электронная почта не может быть пустой")
    @Email(message = "Электронная почта должна быть корректной")
    private String email;

    // Логин пользователя.
    @NotBlank(message = "Логин не может быть пустым")
    // Регулярное выражение для проверки отсутствия пробелов
    @Pattern(regexp = "\\S+", message = "Логин не должен содержать пробелы")
    private String login;

    // Имя, которое будет видно другим пользователям.
    private String name;

    // Дата рождения пользователя.
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    // Идентификаторы друзей пользователя.
    private Set<Integer> friends = new HashSet<>();
}
