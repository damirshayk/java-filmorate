package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сервис для работы с пользователями.
 */
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    /**
     * Создает нового пользователя.
     * @param user пользователь для создания
     * @return созданный пользователь
     */
    public User create(User user) {
        return userStorage.create(user);
    }

    /**
     * Обновляет существующего пользователя.
     * @param user пользователь для обновления
     * @return обновленный пользователь
     */
    public User update(User user) {
        User storedUser = userStorage.findById(user.getId());
        user.setFriends(storedUser.getFriends());
        return userStorage.update(user);
    }

    /**
     * Удаляет пользователя по его ID.
     * @param id ID пользователя
     */
    public void delete(int id) {
        userStorage.delete(id);
    }

    /**
     * Находит пользователя по его ID.
     * @param id ID пользователя
     * @return найденный пользователь
     */
    public User findById(int id) {
        return userStorage.findById(id);
    }

    /**
     * Возвращает всех пользователей.
     * @return коллекция всех пользователей
     */
    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    /**
     * Добавляет друга пользователю.
     * @param id ID пользователя
     * @param friendId ID друга
     */
    public void addFriend(int id, int friendId) {
        User user = userStorage.findById(id);
        User friend = userStorage.findById(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(id);
        userStorage.update(user);
        userStorage.update(friend);
    }

    /**
     * Удаляет друга из списка друзей пользователя.
     * @param id ID пользователя
     * @param friendId ID друга
     */
    public void removeFriend(int id, int friendId) {
        User user = userStorage.findById(id);
        User friend = userStorage.findById(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(id);
        userStorage.update(user);
        userStorage.update(friend);
    }

    /**
     * Возвращает список друзей пользователя.
     * @param id ID пользователя
     * @return список друзей пользователя
     */
    public Collection<User> getFriends(int id) {
        User user = userStorage.findById(id);
        return usersByIds(user.getFriends());
    }

    /**
     * Возвращает список общих друзей двух пользователей.
     * @param id ID первого пользователя
     * @param otherId ID второго пользователя
     * @return список общих друзей
     */
    public Collection<User> getCommonFriends(int id, int otherId) {
        User user = userStorage.findById(id);
        User other = userStorage.findById(otherId);
        Set<Integer> commonIds = user.getFriends().stream()
                .filter(other.getFriends()::contains)
                .collect(Collectors.toSet());
        return usersByIds(commonIds);
    }

    /**
     * Возвращает список пользователей по их ID.
     * @param ids коллекция ID пользователей
     * @return список пользователей
     */
    private List<User> usersByIds(Collection<Integer> ids) {
        return ids.stream()
                .sorted()
                .map(userStorage::findById)
                .toList();
    }
}
