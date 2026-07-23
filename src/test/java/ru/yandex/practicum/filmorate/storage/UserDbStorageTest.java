package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {
    private final UserDbStorage userStorage;

    @Test
    void shouldCreateFindAndListUsers() {
        User created = userStorage.create(makeUser("first"));

        assertThat(created.getId()).isPositive();
        assertThat(userStorage.findById(created.getId()))
                .usingRecursiveComparison()
                .isEqualTo(created);
        assertThat(userStorage.findAll())
                .extracting(User::getId)
                .containsExactly(created.getId());
    }

    @Test
    void shouldUpdateAndDeleteUser() {
        User created = userStorage.create(makeUser("first"));
        created.setName("Новое имя");

        User updated = userStorage.update(created);

        assertThat(updated.getName()).isEqualTo("Новое имя");
        assertThat(userStorage.findById(created.getId()).getName()).isEqualTo("Новое имя");

        userStorage.delete(created.getId());

        assertThatThrownBy(() -> userStorage.findById(created.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldStoreOneWayFriendshipsAndFindCommonFriends() {
        User first = userStorage.create(makeUser("first"));
        User second = userStorage.create(makeUser("second"));
        User common = userStorage.create(makeUser("common"));

        userStorage.addFriend(first.getId(), second.getId());
        userStorage.addFriend(first.getId(), common.getId());
        userStorage.addFriend(second.getId(), common.getId());

        assertThat(userStorage.getFriends(first.getId()))
                .extracting(User::getId)
                .containsExactly(second.getId(), common.getId());
        assertThat(userStorage.getFriends(second.getId()))
                .extracting(User::getId)
                .containsExactly(common.getId());
        assertThat(userStorage.getCommonFriends(first.getId(), second.getId()))
                .extracting(User::getId)
                .containsExactly(common.getId());

        userStorage.removeFriend(first.getId(), second.getId());

        assertThat(userStorage.getFriends(first.getId()))
                .extracting(User::getId)
                .containsExactly(common.getId());
    }

    private User makeUser(String login) {
        User user = new User();
        user.setEmail(login + "@example.com");
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }
}
