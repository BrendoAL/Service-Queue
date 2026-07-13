package com.brendo.queue.controller;

import com.brendo.queue.dto.request.CallNextTicketRequest;
import com.brendo.queue.dto.request.CreateTicketRequest;
import com.brendo.queue.dto.request.TransferTicketRequest;
import com.brendo.queue.dto.response.TicketResponse;
import com.brendo.queue.entity.TicketPriority;
import com.brendo.queue.entity.TicketStatus;
import com.brendo.queue.service.TicketService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TicketControllerTest {

    private final TicketService ticketService = mock(TicketService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new TicketController(ticketService))
        .build();

    @Test
    void createReturnsCreatedTicket() throws Exception {
        when(ticketService.create(any(CreateTicketRequest.class)))
            .thenReturn(new TicketResponse(
                1L,
                "0001",
                TicketStatus.WAITING,
                TicketPriority.PRIORITY,
                null,
                null,
                null,
                null,
                null
            ));

        mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateTicketRequest(TicketPriority.PRIORITY))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.number").value("0001"))
            .andExpect(jsonPath("$.priority").value("PRIORITY"))
            .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void getByIdReturnsTicket() throws Exception {
        when(ticketService.getById(1L))
            .thenReturn(new TicketResponse(
                1L,
                "0001",
                TicketStatus.WAITING,
                TicketPriority.NORMAL,
                null,
                null,
                null,
                null,
                null
            ));

        mockMvc.perform(get("/api/tickets/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.number").value("0001"));
    }

    @Test
    void listReturnsTicketsFilteredByStatus() throws Exception {
        when(ticketService.list(TicketStatus.WAITING))
            .thenReturn(List.of(new TicketResponse(
                1L,
                "0001",
                TicketStatus.WAITING,
                TicketPriority.NORMAL,
                null,
                null,
                null,
                null,
                null
            )));

        mockMvc.perform(get("/api/tickets").param("status", "WAITING"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].number").value("0001"))
            .andExpect(jsonPath("$[0].status").value("WAITING"));
    }

    @Test
    void callNextReturnsCalledTicket() throws Exception {
        when(ticketService.callNext(any(CallNextTicketRequest.class)))
            .thenReturn(new TicketResponse(
                1L,
                "0001",
                TicketStatus.CALLED,
                TicketPriority.NORMAL,
                10L,
                "Guiche 1",
                null,
                null,
                null
            ));

        mockMvc.perform(post("/api/tickets/next/call")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CallNextTicketRequest(10L))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CALLED"))
            .andExpect(jsonPath("$.counterId").value(10))
            .andExpect(jsonPath("$.counterName").value("Guiche 1"));
    }

    @Test
    void completeReturnsCompletedTicket() throws Exception {
        when(ticketService.complete(1L))
            .thenReturn(new TicketResponse(
                1L,
                "0001",
                TicketStatus.COMPLETED,
                TicketPriority.NORMAL,
                10L,
                "Guiche 1",
                null,
                null,
                null
            ));

        mockMvc.perform(post("/api/tickets/1/complete"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void startServiceReturnsInServiceTicket() throws Exception {
        when(ticketService.startService(1L))
            .thenReturn(new TicketResponse(
                1L,
                "0001",
                TicketStatus.IN_SERVICE,
                TicketPriority.NORMAL,
                10L,
                "Guiche 1",
                null,
                null,
                null
            ));

        mockMvc.perform(post("/api/tickets/1/start"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("IN_SERVICE"));
    }

    @Test
    void transferReturnsTicketWithNewCounter() throws Exception {
        when(ticketService.transfer(org.mockito.ArgumentMatchers.eq(1L), any(TransferTicketRequest.class)))
            .thenReturn(new TicketResponse(
                1L,
                "0001",
                TicketStatus.CALLED,
                TicketPriority.NORMAL,
                20L,
                "Guiche 2",
                null,
                null,
                null
            ));

        mockMvc.perform(post("/api/tickets/1/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TransferTicketRequest(20L))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.counterId").value(20))
            .andExpect(jsonPath("$.counterName").value("Guiche 2"));
    }

    @Test
    void cancelReturnsCancelledTicket() throws Exception {
        when(ticketService.cancel(1L))
            .thenReturn(new TicketResponse(
                1L,
                "0001",
                TicketStatus.CANCELLED,
                TicketPriority.NORMAL,
                null,
                null,
                null,
                null,
                null
            ));

        mockMvc.perform(post("/api/tickets/1/cancel"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
