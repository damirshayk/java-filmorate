package ru.yandex.practicum.filmorate.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Ошибка для неверных данных из запроса. Клиент получит ответ 400 Bad Request.
@ResponseStatus(HttpStatus.BAD_REQUEST) // Это подсказка ИИ
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
