package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InMemoryUserStorageTest {
    private UserStorage storage;

    @BeforeEach
    void setUp() {
        storage = new InMemoryUserStorage();
    }

    @Test
    void shouldCreateUser() {
        User user = makeUser("first");

        User created = storage.create(user);

        assertEquals(1, created.getId());
        assertEquals("Name", created.getName());
        assertEquals(created, storage.findById(created.getId()));
        assertEquals(1, storage.findAll().size());
    }

    @Test
    void shouldUpdateAndDeleteUser() {
        User created = storage.create(makeUser("first"));
        created.setName("Новое имя");

        User updated = storage.update(created);

        assertEquals("Новое имя", updated.getName());
        storage.delete(created.getId());
        assertEquals(0, storage.findAll().size());
    }

    @Test
    void shouldThrowWhenUserDoesNotExist() {
        User unknown = makeUser("unknown");
        unknown.setId(99);

        assertThrows(NotFoundException.class, () -> storage.findById(99));
        assertThrows(NotFoundException.class, () -> storage.update(unknown));
        assertThrows(NotFoundException.class, () -> storage.delete(99));
    }

    private User makeUser(String login) {
        User user = new User();
        user.setEmail(login + "@example.com");
        user.setLogin(login);
        user.setName("Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }
}
