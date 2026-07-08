package com.brendo.queue.controller;

import com.brendo.queue.dto.response.QueueStatusResponse;
import com.brendo.queue.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class QueueControllerTest {

    private final TicketService ticketService = mock(TicketService.class);
    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new QueueController(ticketService))
        .build();

    @Test
    void statusReturnsQueueStatus() throws Exception {
        when(ticketService.queueStatus()).thenReturn(new QueueStatusResponse(3, 1, 0, List.of()));

        mockMvc.perform(get("/api/queue/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.waiting").value(3))
            .andExpect(jsonPath("$.called").value(1))
            .andExpect(jsonPath("$.inService").value(0));
    }
}
