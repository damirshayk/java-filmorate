package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Тесты проверки пользователей.
 */
class UserControllerTest {
    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController();
    }

    @Test
    @DisplayName("Пользователь с правильными данными создается")
    void shouldCreateUserWithValidData() {
        User user = makeValidUser();

        User created = controller.create(user);

        assertEquals(1, created.getId());
        assertEquals("login", created.getLogin());
    }

    @Test
    @DisplayName("Пустой запрос пользователя не проходит проверку")
    void shouldThrowExceptionWhenUserRequestIsEmpty() {
        User user = new User();

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    @DisplayName("Пустая почта не проходит проверку")
    void shouldThrowExceptionWhenEmailIsBlank() {
        User user = makeValidUser();
        user.setEmail("");

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    @DisplayName("Почта без символа @ не проходит проверку")
    void shouldThrowExceptionWhenEmailDoesNotContainAtSign() {
        User user = makeValidUser();
        user.setEmail("mail.example.com");

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    @DisplayName("Пустой логин не проходит проверку")
    void shouldThrowExceptionWhenLoginIsBlank() {
        User user = makeValidUser();
        user.setLogin("   ");

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    @DisplayName("Логин с пробелом не проходит проверку")
    void shouldThrowExceptionWhenLoginContainsSpace() {
        User user = makeValidUser();
        user.setLogin("my login");

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    @DisplayName("Если имя пустое, вместо него используется логин")
    void shouldUseLoginAsNameWhenNameIsBlank() {
        User user = makeValidUser();
        user.setName("");

        User created = controller.create(user);

        assertEquals("login", created.getName());
    }

    @Test
    @DisplayName("Если имя не передано, вместо него используется логин")
    void shouldUseLoginAsNameWhenNameIsNull() {
        User user = makeValidUser();
        user.setName(null);

        User created = controller.create(user);

        assertEquals("login", created.getName());
    }

    @Test
    @DisplayName("Дата рождения сегодня проходит проверку")
    void shouldCreateUserWhenBirthdayIsToday() {
        User user = makeValidUser();
        user.setBirthday(LocalDate.now());

        assertDoesNotThrow(() -> controller.create(user));
    }

    @Test
    @DisplayName("Дата рождения в будущем не проходит проверку")
    void shouldThrowExceptionWhenBirthdayIsInFuture() {
        User user = makeValidUser();
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    /**
     * Создает пользователя с правильными данными для тестов.
     */
    private User makeValidUser() {
        User user = new User();
        user.setEmail("mail@example.com");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }
}
