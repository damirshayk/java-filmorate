package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.ErrorHandler;

import java.time.LocalDate;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FilmorateApiTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSupportUserAndFriendEndpoints() throws Exception {
        int first = createUser("first");
        int second = createUser("second");
        int common = createUser("common");

        mockMvc.perform(get("/users/{id}", first))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("first"));
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
        mockMvc.perform(put("/users").contentType(APPLICATION_JSON).content(userJson(first, "first", "Новое имя")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Новое имя"));

        addFriend(first, second);
        addFriend(first, common);
        addFriend(second, common);

        mockMvc.perform(get("/users/{id}/friends", first))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(get("/users/{id}/friends/common/{otherId}", first, second))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(common));

        mockMvc.perform(delete("/users/{id}/friends/{friendId}", first, second))
                .andExpect(status().isOk());
        mockMvc.perform(get("/users/{id}/friends", first))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == %d)]".formatted(second)).isEmpty());
    }

    @Test
    void shouldSupportFilmLikeAndPopularEndpoints() throws Exception {
        int user = createUser("user");
        int secondUser = createUser("second-user");
        int firstFilm = createFilm("First");
        int secondFilm = createFilm("Second");
        for (int index = 3; index <= 11; index++) {
            createFilm("Film " + index);
        }

        mockMvc.perform(get("/films/{id}", firstFilm))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("First"));
        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(11));
        mockMvc.perform(put("/films").contentType(APPLICATION_JSON).content(filmJson(firstFilm, "Updated")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));

        mockMvc.perform(put("/films/{id}/like/{userId}", firstFilm, user)).andExpect(status().isOk());
        mockMvc.perform(put("/films/{id}/like/{userId}", secondFilm, user)).andExpect(status().isOk());
        mockMvc.perform(put("/films/{id}/like/{userId}", secondFilm, secondUser)).andExpect(status().isOk());
        mockMvc.perform(get("/films/popular")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(10))
                .andExpect(jsonPath("$[0].id").value(secondFilm));
        mockMvc.perform(get("/films/popular").param("count", "2")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(secondFilm))
                .andExpect(jsonPath("$[1].id").value(firstFilm));

        mockMvc.perform(delete("/films/{id}/like/{userId}", secondFilm, secondUser))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnRequiredErrorStatuses() throws Exception {
        String oldFilmJson = "{\"name\":\"Old\",\"description\":\"Description\","
                + "\"releaseDate\":\"1895-12-27\",\"duration\":100}";
        mockMvc.perform(post("/films").contentType(APPLICATION_JSON).content(oldFilmJson))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/users/999")).andExpect(status().isNotFound());
        mockMvc.perform(get("/films/999")).andExpect(status().isNotFound());
        mockMvc.perform(get("/films/popular").param("count", "0")).andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn500ForUnexpectedException() throws Exception {
        MockMvc standalone = MockMvcBuilders.standaloneSetup(new ThrowingController())
                .setControllerAdvice(new ErrorHandler())
                .build();

        standalone.perform(get("/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.Ошибка").value("Внутренняя ошибка сервера"));
    }

    private int createUser(String login) throws Exception {
        String response = mockMvc.perform(post("/users").contentType(APPLICATION_JSON)
                        .content(userJson(0, login, login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asInt();
    }

    private int createFilm(String name) throws Exception {
        String response = mockMvc.perform(post("/films").contentType(APPLICATION_JSON)
                        .content(filmJson(0, name)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asInt();
    }

    private void addFriend(int id, int friendId) throws Exception {
        mockMvc.perform(put("/users/{id}/friends/{friendId}", id, friendId))
                .andExpect(status().isOk());
    }

    private String userJson(int id, String login, String name) {
        return ("{\"id\":%d,\"email\":\"%s@example.com\",\"login\":\"%s\","
                + "\"name\":\"%s\",\"birthday\":\"2000-01-01\"}")
                .formatted(id, login, login, name);
    }

    private String filmJson(int id, String name) {
        return ("{\"id\":%d,\"name\":\"%s\",\"description\":\"Description\","
                + "\"releaseDate\":\"%s\",\"duration\":100}")
                .formatted(id, name, LocalDate.of(2000, 1, 1));
    }

    @RestController
    private static class ThrowingController {
        @GetMapping("/unexpected")
        void unexpected() {
            throw new IllegalStateException("Неожиданная ошибка");
        }
    }
}
