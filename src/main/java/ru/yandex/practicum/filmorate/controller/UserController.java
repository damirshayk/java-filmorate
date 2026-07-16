package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для работы с пользователями.
 */
@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    // Временное хранилище пользователей, пока нет бд.
    private final Map<Integer, User> users = new HashMap<>();

    private int nextId = 1;

    /**
     * Создает нового пользователя.
     */
    @PostMapping
    public User create(@Valid @RequestBody User user) {
        validate(user);
        user.setId(nextId++);
        users.put(user.getId(), user);
        log.info("Добавлен пользователь: id={}, login={}", user.getId(), user.getLogin());
        return user;
    }

    /**
     * Обновляет пользователя, который уже есть в хранилище.
     */
    @PutMapping
    public User update(@Valid @RequestBody User user) {
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден");
        }

        validate(user);
        users.put(user.getId(), user);
        log.info("Обновлен пользователь: id={}, login={}", user.getId(), user.getLogin());
        return user;
    }

    /**
     * Возвращает всех пользователей.
     */
    @GetMapping
    public Collection<User> findAll() {
        log.info("Запрос на получение всех пользователей, количество={}", users.size());
        return users.values();
    }

    /**
     * Проверяет, что данные пользователя подходят под правила задания.
     */
    private void validate(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Электронная почта должна быть непустой и содержать @");
        }

        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}
