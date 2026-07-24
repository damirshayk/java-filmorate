package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

/*
 * Интерфейс для хранения пользователей.
 */
public interface UserStorage {
    User create(User user);

    User update(User user);

    void delete(int id);

    User findById(int id);

    Collection<User> findAll();

    void addFriend(int userId, int friendId);

    void removeFriend(int userId, int friendId);

    Collection<User> getFriends(int userId);

    Collection<User> getCommonFriends(int userId, int otherId);
}
