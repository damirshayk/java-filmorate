package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

/**
 * Сервис для работы с пользователями.
 */
@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    /**
     * Создает нового пользователя.
     *
     * @param user пользователь для создания
     * @return созданный пользователь
     */
    public User create(User user) {
        setDefaultName(user);
        return userStorage.create(user);
    }

    /**
     * Обновляет существующего пользователя.
     *
     * @param user пользователь для обновления
     * @return обновленный пользователь
     */
    public User update(User user) {
        setDefaultName(user);
        return userStorage.update(user);
    }

    /**
     * Удаляет пользователя по его ID.
     *
     * @param id ID пользователя
     */
    public void delete(int id) {
        userStorage.delete(id);
    }

    /**
     * Находит пользователя по его ID.
     *
     * @param id ID пользователя
     * @return найденный пользователь
     */
    public User findById(int id) {
        return userStorage.findById(id);
    }

    /**
     * Возвращает всех пользователей.
     *
     * @return коллекция всех пользователей
     */
    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    /**
     * Добавляет друга пользователю.
     *
     * @param id       ID пользователя
     * @param friendId ID друга
     */
    public void addFriend(int id, int friendId) {
        userStorage.addFriend(id, friendId);
    }

    /**
     * Удаляет друга из списка друзей пользователя.
     *
     * @param id       ID пользователя
     * @param friendId ID друга
     */
    public void removeFriend(int id, int friendId) {
        userStorage.removeFriend(id, friendId);
    }

    /**
     * Возвращает список друзей пользователя.
     *
     * @param id ID пользователя
     * @return список друзей пользователя
     */
    public Collection<User> getFriends(int id) {
        return userStorage.getFriends(id);
    }

    /**
     * Возвращает список общих друзей двух пользователей.
     *
     * @param id      ID первого пользователя
     * @param otherId ID второго пользователя
     * @return список общих друзей
     */
    public Collection<User> getCommonFriends(int id, int otherId) {
        return userStorage.getCommonFriends(id, otherId);
    }

    /**
     * Устанавливает имя пользователя по умолчанию, если оно не задано.
     *
     * @param user пользователь
     */
    private void setDefaultName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
