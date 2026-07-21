package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InMemoryFilmStorageTest {
    private FilmStorage storage;

    @BeforeEach
    void setUp() {
        storage = new InMemoryFilmStorage();
    }

    @Test
    void shouldCreateFilm() {
        Film film = makeFilm();

        Film created = storage.create(film);

        assertEquals(1, created.getId());
        assertEquals(created, storage.findById(created.getId()));
        assertEquals(1, storage.findAll().size());
    }

    @Test
    void shouldUpdateAndDeleteFilm() {
        Film created = storage.create(makeFilm());
        created.setName("Новое название");

        Film updated = storage.update(created);

        assertEquals("Новое название", updated.getName());
        storage.delete(created.getId());
        assertEquals(0, storage.findAll().size());
    }

    @Test
    void shouldThrowWhenFilmDoesNotExist() {
        Film unknown = makeFilm();
        unknown.setId(99);

        assertThrows(NotFoundException.class, () -> storage.findById(99));
        assertThrows(NotFoundException.class, () -> storage.update(unknown));
        assertThrows(NotFoundException.class, () -> storage.delete(99));
    }

    private Film makeFilm() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);
        return film;
    }
}
