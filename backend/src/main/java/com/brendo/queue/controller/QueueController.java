package com.brendo.queue.controller;

import com.brendo.queue.dto.response.QueueStatusResponse;
import com.brendo.queue.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/queue")
public class QueueController {

    private final TicketService ticketService;

    public QueueController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/status")
    public ResponseEntity<QueueStatusResponse> status() {
        return ResponseEntity.ok(ticketService.queueStatus());
    }
}
