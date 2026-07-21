package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * Реализация UserStorage, которая хранит пользователей в памяти.
 */
@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    // Хранение пользователей в ConcurrentHashMap для потокобезопасности
    private final Map<Integer, User> users = new ConcurrentHashMap<>();
    // AtomicInteger для генерации уникальных идентификаторов пользователей
    private final AtomicInteger nextId = new AtomicInteger(1);

    @Override
    public User create(User user) {
        user.setId(nextId.getAndIncrement());
        User savedUser = copyUser(user);
        users.put(savedUser.getId(), savedUser);
        log.info("Добавлен пользователь: id={}, login={}", user.getId(), user.getLogin());
        return copyUser(user);
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден");
        }

        User savedUser = users.get(user.getId());
        users.put(savedUser.getId(), copyUser(user));

        log.info("Обновлен пользователь: id={}, login={}", user.getId(), user.getLogin());
        return copyUser(user);
    }

    @Override
    public void delete(int id) {
        findById(id);
        users.remove(id);
        log.info("Удален пользователь: id={}", id);
    }

    @Override
    public User findById(int id) {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        return copyUser(user);
    }

    @Override
    public Collection<User> findAll() {
        ArrayList<User> userCopies = new ArrayList<>();

        for (User user : users.values()) {
            userCopies.add(copyUser(user));
        }

        // Сортируем пользователей по ID перед возвратом
        userCopies.sort(Comparator.comparingInt(User::getId));
        return userCopies;
    }

    // Создаем копию объекта User, чтобы избежать изменения оригинального объекта при обновлении
    private User copyUser(User user) {
        User copy = new User();
        copy.setId(user.getId());
        copy.setEmail(user.getEmail());
        copy.setLogin(user.getLogin());
        copy.setName(user.getName());
        copy.setBirthday(user.getBirthday());
        copy.setFriends(new HashSet<>(user.getFriends()));
        return copy;
    }
}
