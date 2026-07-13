package com.brendo.queue.controller;

import com.brendo.queue.dto.request.CallNextTicketRequest;
import com.brendo.queue.dto.request.CreateTicketRequest;
import com.brendo.queue.dto.request.TransferTicketRequest;
import com.brendo.queue.dto.response.TicketResponse;
import com.brendo.queue.entity.TicketStatus;
import com.brendo.queue.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> create(@RequestBody(required = false) CreateTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> list(@RequestParam(required = false) TicketStatus status) {
        return ResponseEntity.ok(ticketService.list(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getById(id));
    }

    @PostMapping("/next/call")
    public ResponseEntity<TicketResponse> callNext(@Valid @RequestBody CallNextTicketRequest request) {
        return ResponseEntity.ok(ticketService.callNext(request));
    }

    @PostMapping("/{id}/recall")
    public ResponseEntity<TicketResponse> recall(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.recall(id));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<TicketResponse> startService(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.startService(id));
    }

    @PostMapping("/{id}/transfer")
    public ResponseEntity<TicketResponse> transfer(
            @PathVariable Long id,
            @Valid @RequestBody TransferTicketRequest request) {
        return ResponseEntity.ok(ticketService.transfer(id, request));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<TicketResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.cancel(id));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<TicketResponse> complete(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.complete(id));
    }
}
