package com.brendo.queue.controller;

import com.brendo.queue.dto.request.CounterRequest;
import com.brendo.queue.dto.response.CounterResponse;
import com.brendo.queue.service.CounterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CounterControllerTest {

    private final CounterService counterService = mock(CounterService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new CounterController(counterService))
        .build();

    @Test
    void listReturnsCounters() throws Exception {
        when(counterService.list(true))
            .thenReturn(List.of(new CounterResponse(1L, "Guiche 1", true, null)));

        mockMvc.perform(get("/api/counters").param("active", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Guiche 1"))
            .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void createReturnsCreatedCounter() throws Exception {
        when(counterService.create(any(CounterRequest.class)))
            .thenReturn(new CounterResponse(1L, "Guiche 1", true, null));

        mockMvc.perform(post("/api/counters")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CounterRequest("Guiche 1"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Guiche 1"))
            .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void deactivateReturnsUpdatedCounter() throws Exception {
        when(counterService.deactivate(1L))
            .thenReturn(new CounterResponse(1L, "Guiche 1", false, null));

        mockMvc.perform(patch("/api/counters/1/deactivate"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.active").value(false));
    }
}
