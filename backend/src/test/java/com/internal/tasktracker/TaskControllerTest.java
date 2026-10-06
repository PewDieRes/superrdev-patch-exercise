package com.internal.tasktracker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void noFiltersExcludesArchivedTasks() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(47));
    }

    @Test
    void searchAppliesStatusFilterToEveryMatch() throws Exception {
        mockMvc.perform(get("/api/tasks").param("q", "api").param("status", "DONE").param("pageSize", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].status").value(org.hamcrest.Matchers.everyItem(
                        org.hamcrest.Matchers.is("DONE"))))
                .andExpect(jsonPath("$.items[*].archived").value(org.hamcrest.Matchers.everyItem(
                        org.hamcrest.Matchers.is(false))))
                .andExpect(jsonPath("$.items[?(@.id == 20 || @.id == 21)]").isEmpty());
    }

    @Test
    void searchNeverReturnsArchivedTasks() throws Exception {
        mockMvc.perform(get("/api/tasks").param("q", "legacy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void invalidParametersReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/tasks").param("status", "CLOSED"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/tasks").param("page", "0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/tasks").param("pageSize", "0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/tasks").param("pageSize", "101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void likeWildcardsAreMatchedLiterally() throws Exception {
        mockMvc.perform(get("/api/tasks").param("q", "%"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void paginationCoversAllTasksWithoutOverlap() throws Exception {
        Set<Long> ids = new HashSet<>();
        for (int page = 1; page <= 5; page++) {
            String body = mockMvc.perform(get("/api/tasks").param("pageSize", "10").param("page", String.valueOf(page)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            JsonNode items = objectMapper.readTree(body).get("items");
            assertEquals(page < 5 ? 10 : 7, items.size());
            for (JsonNode item : items) {
                assertTrue(ids.add(item.get("id").asLong()), "duplicate id " + item.get("id"));
            }
        }
        assertEquals(47, ids.size());
    }

    @Test
    void firstPageStartsWithNewestTask() throws Exception {
        mockMvc.perform(get("/api/tasks").param("pageSize", "10").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(49));
    }

    @Test
    void statusFilterIsCaseInsensitive() throws Exception {
        mockMvc.perform(get("/api/tasks").param("status", "open"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].status").value(org.hamcrest.Matchers.everyItem(
                        org.hamcrest.Matchers.is("OPEN"))));
    }
}
