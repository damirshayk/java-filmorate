package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тесты проверки пользователей.
 */
@SpringBootTest
@Transactional
class UserControllerTest {
    @Autowired
    private UserController controller;
    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("Пользователь с правильными данными создается")
    void shouldCreateUserWithValidData() {
        User user = makeValidUser();

        User created = controller.create(user);

        assertTrue(created.getId() > 0);
        assertEquals("login", created.getLogin());
    }

    @Test
    @DisplayName("Пустой запрос пользователя не проходит проверку")
    void shouldFailValidationWhenUserRequestIsEmpty() {
        User user = new User();

        assertFalse(validator.validate(user).isEmpty());
    }

    @Test
    @DisplayName("Пустая почта не проходит проверку")
    void shouldFailValidationWhenEmailIsBlank() {
        User user = makeValidUser();
        user.setEmail("");

        assertFalse(validator.validate(user).isEmpty());
    }

    @Test
    @DisplayName("Почта без символа @ не проходит проверку")
    void shouldFailValidationWhenEmailDoesNotContainAtSign() {
        User user = makeValidUser();
        user.setEmail("mail.example.com");

        assertFalse(validator.validate(user).isEmpty());
    }

    @Test
    @DisplayName("Пустой логин не проходит проверку")
    void shouldFailValidationWhenLoginIsBlank() {
        User user = makeValidUser();
        user.setLogin("   ");

        assertFalse(validator.validate(user).isEmpty());
    }

    @Test
    @DisplayName("Логин с пробелом не проходит проверку")
    void shouldFailValidationWhenLoginContainsSpace() {
        User user = makeValidUser();
        user.setLogin("my login");

        assertFalse(validator.validate(user).isEmpty());
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
    void shouldFailValidationWhenBirthdayIsInFuture() {
        User user = makeValidUser();
        user.setBirthday(LocalDate.now().plusDays(1));

        assertFalse(validator.validate(user).isEmpty());
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
