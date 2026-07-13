package com.brendo.queue.controller;

import com.brendo.queue.dto.request.CreateUserRequest;
import com.brendo.queue.dto.request.UpdateUserRequest;
import com.brendo.queue.dto.response.UserResponse;
import com.brendo.queue.service.UserService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

    private final UserService userService = mock(UserService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new UserController(userService))
        .build();

    @Test
    void listReturnsUsers() throws Exception {
        when(userService.list("ATENDENTE"))
            .thenReturn(List.of(new UserResponse(1L, "attendant", "ATENDENTE", 10L, null)));

        mockMvc.perform(get("/api/users").param("role", "ATENDENTE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].username").value("attendant"))
            .andExpect(jsonPath("$[0].role").value("ATENDENTE"))
            .andExpect(jsonPath("$[0].counterId").value(10));
    }

    @Test
    void createReturnsCreatedUser() throws Exception {
        when(userService.create(any(CreateUserRequest.class)))
            .thenReturn(new UserResponse(1L, "attendant", "ATENDENTE", 10L, null));

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new CreateUserRequest("attendant", "password123", "ATENDENTE", 10L)
                )))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.username").value("attendant"))
            .andExpect(jsonPath("$.role").value("ATENDENTE"));
    }

    @Test
    void updateReturnsUpdatedUser() throws Exception {
        when(userService.update(org.mockito.ArgumentMatchers.eq(1L), any(UpdateUserRequest.class)))
            .thenReturn(new UserResponse(1L, "admin", "ADMIN", null, null));

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new UpdateUserRequest("admin", null, "ADMIN", null)
                )))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("admin"))
            .andExpect(jsonPath("$.role").value("ADMIN"));
    }
}
