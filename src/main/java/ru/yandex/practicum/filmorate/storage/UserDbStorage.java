package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Реализация хранилища пользователей с использованием базы данных.
 */
@Slf4j
@Component("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private static final String SELECT_USER = """
            SELECT user_id, email, login, name, birthday
            FROM users
            """;
    private final JdbcTemplate jdbc;
    private final UserRowMapper mapper;

    @Override
    public User create(User user) {
        String sql = """
                INSERT INTO users(email, login, name, birthday)
                VALUES (?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, user.getEmail());
            statement.setString(2, user.getLogin());
            statement.setString(3, user.getName());
            statement.setObject(4, user.getBirthday());
            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Не удалось получить идентификатор созданного пользователя");
        }
        user.setId(key.intValue());
        log.info("Добавлен пользователь в БД: id={}, login={}", user.getId(), user.getLogin());
        return findById(user.getId());
    }

    @Override
    public User update(User user) {
        int updated = jdbc.update("""
                        UPDATE users
                        SET email = ?, login = ?, name = ?, birthday = ?
                        WHERE user_id = ?
                        """,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());
        if (updated == 0) {
            throw userNotFound(user.getId());
        }
        log.info("Обновлен пользователь в БД: id={}, login={}", user.getId(), user.getLogin());
        return findById(user.getId());
    }

    @Override
    public void delete(int id) {
        findById(id);
        jdbc.update("DELETE FROM users WHERE user_id = ?", id);
        log.info("Удален пользователь из БД: id={}", id);
    }

    @Override
    public User findById(int id) {
        List<User> users = jdbc.query(
                SELECT_USER + " WHERE user_id = ?",
                mapper,
                id);
        if (users.isEmpty()) {
            throw userNotFound(id);
        }
        loadFriends(users);
        return users.getFirst();
    }

    @Override
    public Collection<User> findAll() {
        List<User> users = jdbc.query(
                SELECT_USER + " ORDER BY user_id",
                mapper);
        loadFriends(users);
        return users;
    }

    @Override
    @Transactional
    public void addFriend(int userId, int friendId) {
        findById(userId);
        findById(friendId);

        Integer reverseCount = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM friendships
                WHERE requester_id = ? AND addressee_id = ?
                """, Integer.class, friendId, userId);
        boolean reverseExists = reverseCount != null && reverseCount > 0;
        FriendshipStatus status = reverseExists
                ? FriendshipStatus.CONFIRMED
                : FriendshipStatus.UNCONFIRMED;

        jdbc.update("""
                MERGE INTO friendships(requester_id, addressee_id, status)
                KEY(requester_id, addressee_id)
                VALUES (?, ?, ?)
                """, userId, friendId, status.name());

        if (reverseExists) {
            jdbc.update("""
                    UPDATE friendships
                    SET status = ?
                    WHERE requester_id = ? AND addressee_id = ?
                    """, FriendshipStatus.CONFIRMED.name(), friendId, userId);
        }
    }

    @Override
    @Transactional
    public void removeFriend(int userId, int friendId) {
        findById(userId);
        findById(friendId);
        jdbc.update("""
                DELETE FROM friendships
                WHERE requester_id = ? AND addressee_id = ?
                """, userId, friendId);
        jdbc.update("""
                UPDATE friendships
                SET status = ?
                WHERE requester_id = ? AND addressee_id = ?
                """, FriendshipStatus.UNCONFIRMED.name(), friendId, userId);
    }

    @Override
    public Collection<User> getFriends(int userId) {
        findById(userId);
        List<User> friends = jdbc.query("""
                        SELECT u.user_id, u.email, u.login, u.name, u.birthday
                        FROM users u
                        JOIN friendships f ON f.addressee_id = u.user_id
                        WHERE f.requester_id = ?
                        ORDER BY u.user_id
                        """,
                mapper,
                userId);
        loadFriends(friends);
        return friends;
    }

    @Override
    public Collection<User> getCommonFriends(int userId, int otherId) {
        findById(userId);
        findById(otherId);
        List<User> friends = jdbc.query("""
                        SELECT u.user_id, u.email, u.login, u.name, u.birthday
                        FROM users u
                        JOIN friendships first_friend ON first_friend.addressee_id = u.user_id
                        JOIN friendships second_friend ON second_friend.addressee_id = u.user_id
                        WHERE first_friend.requester_id = ?
                          AND second_friend.requester_id = ?
                        ORDER BY u.user_id
                        """,
                mapper,
                userId,
                otherId);
        loadFriends(friends);
        return friends;
    }

    // Загружает идентификаторы друзей для списка пользователей одним запросом.
    private void loadFriends(Collection<User> users) {
        if (users.isEmpty()) {
            return;
        }

        Map<Integer, User> usersById = new LinkedHashMap<>();
        for (User user : users) {
            user.setFriends(new LinkedHashSet<>());
            usersById.put(user.getId(), user);
        }

        String placeholders = String.join(", ", Collections.nCopies(usersById.size(), "?"));
        Object[] userIds = usersById.keySet().toArray();
        jdbc.query("""
                        SELECT requester_id, addressee_id
                        FROM friendships
                        WHERE requester_id IN (%s)
                        ORDER BY requester_id, addressee_id
                        """.formatted(placeholders),
                resultSet -> {
                    usersById.get(resultSet.getInt("requester_id"))
                            .getFriends()
                            .add(resultSet.getInt("addressee_id"));
                },
                userIds);
    }

    // Возвращает исключение NotFoundException с сообщением о том,
    // что пользователь с указанным идентификатором не найден.
    private NotFoundException userNotFound(int id) {
        return new NotFoundException("Пользователь с id = " + id + " не найден");
    }
}
