package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CatalogControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnGenresAndMpaRatings() throws Exception {
        mockMvc.perform(get("/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6))
                .andExpect(jsonPath("$[0].name").value("Комедия"))
                .andExpect(jsonPath("$[5].name").value("Боевик"));

        mockMvc.perform(get("/genres/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Мультфильм"));

        mockMvc.perform(get("/mpa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].name").value("G"))
                .andExpect(jsonPath("$[4].name").value("NC-17"));

        mockMvc.perform(get("/mpa/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("PG-13"));
    }

    @Test
    void shouldReturnNotFoundForUnknownCatalogIds() throws Exception {
        mockMvc.perform(get("/genres/9999"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/mpa/9999"))
                .andExpect(status().isNotFound());
    }
}
