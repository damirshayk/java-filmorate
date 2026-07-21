package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserServiceTest {
    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(new InMemoryUserStorage());
    }

    @Test
    void shouldAddFriendSymmetricallyWithoutDuplicates() {
        User first = service.create(makeUser("first"));
        User second = service.create(makeUser("second"));

        service.addFriend(first.getId(), second.getId());
        service.addFriend(first.getId(), second.getId());

        assertEquals(1, service.findById(first.getId()).getFriends().size());
        assertTrue(service.findById(first.getId()).getFriends().contains(second.getId()));
        assertTrue(service.findById(second.getId()).getFriends().contains(first.getId()));
        User friend = service.getFriends(first.getId()).iterator().next();
        assertEquals(second.getId(), friend.getId());
        assertTrue(friend.getFriends().contains(first.getId()));
    }

    @Test
    void shouldRemoveFriendSymmetrically() {
        User first = service.create(makeUser("first"));
        User second = service.create(makeUser("second"));
        service.addFriend(first.getId(), second.getId());

        service.removeFriend(first.getId(), second.getId());

        assertTrue(service.findById(first.getId()).getFriends().isEmpty());
        assertTrue(service.findById(second.getId()).getFriends().isEmpty());
    }

    @Test
    void shouldFindCommonFriends() {
        User first = service.create(makeUser("first"));
        User second = service.create(makeUser("second"));
        User common = service.create(makeUser("common"));
        service.addFriend(first.getId(), common.getId());
        service.addFriend(second.getId(), common.getId());

        assertEquals(1, service.getCommonFriends(first.getId(), second.getId()).size());
        User foundCommon = service.getCommonFriends(first.getId(), second.getId()).iterator().next();
        assertEquals(common.getId(), foundCommon.getId());
        assertTrue(foundCommon.getFriends().contains(first.getId()));
        assertTrue(foundCommon.getFriends().contains(second.getId()));
    }

    @Test
    void shouldThrowWhenAnyUserDoesNotExist() {
        User first = service.create(makeUser("first"));

        assertThrows(NotFoundException.class, () -> service.findById(99));
        assertThrows(NotFoundException.class, () -> service.addFriend(first.getId(), 99));
        assertThrows(NotFoundException.class, () -> service.removeFriend(99, first.getId()));
        assertThrows(NotFoundException.class, () -> service.getCommonFriends(first.getId(), 99));
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
