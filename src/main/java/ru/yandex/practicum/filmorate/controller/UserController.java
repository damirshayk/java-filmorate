package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

/**
 * Контроллер для работы с пользователями.
 */
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Создает нового пользователя.
     *
     * @param user пользователь для создания
     * @return созданный пользователь
     */
    @PostMapping
    public User create(@Valid @RequestBody User user) {
        return userService.create(user);
    }

    /**
     * Обновляет существующего пользователя.
     *
     * @param user пользователь для обновления
     * @return обновленный пользователь
     */
    @PutMapping
    public User update(@Valid @RequestBody User user) {
        return userService.update(user);
    }

    /**
     * Возвращает список всех пользователей.
     *
     * @return список пользователей
     */
    @GetMapping
    public Collection<User> findAll() {
        return userService.findAll();
    }

    /**
     * Возвращает пользователя по его ID.
     *
     * @param id ID пользователя
     * @return пользователь
     */
    @GetMapping("/{id}")
    public User findById(@PathVariable int id) {
        return userService.findById(id);
    }

    /**
     * Добавляет пользователя в друзья.
     *
     * @param id       ID пользователя
     * @param friendId ID друга
     */
    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable int id, @PathVariable int friendId) {
        userService.addFriend(id, friendId);
    }

    /**
     * Удаляет пользователя из друзей.
     *
     * @param id       ID пользователя
     * @param friendId ID друга
     */
    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable int id, @PathVariable int friendId) {
        userService.removeFriend(id, friendId);
    }

    /**
     * Возвращает список друзей пользователя.
     *
     * @param id ID пользователя
     * @return список друзей
     */
    @GetMapping("/{id}/friends")
    public Collection<User> getFriends(@PathVariable int id) {
        return userService.getFriends(id);
    }

    /**
     * Возвращает список общих друзей двух пользователей.
     *
     * @param id      ID первого пользователя
     * @param otherId ID второго пользователя
     * @return список общих друзей
     */
    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getCommonFriends(@PathVariable int id, @PathVariable int otherId) {
        return userService.getCommonFriends(id, otherId);
    }
}
