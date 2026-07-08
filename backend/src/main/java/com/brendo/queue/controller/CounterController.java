package com.brendo.queue.controller;

import com.brendo.queue.dto.request.CounterRequest;
import com.brendo.queue.dto.response.CounterResponse;
import com.brendo.queue.service.CounterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/counters")
public class CounterController {

    private final CounterService counterService;

    public CounterController(CounterService counterService) {
        this.counterService = counterService;
    }

    @GetMapping
    public ResponseEntity<List<CounterResponse>> list(@RequestParam(required = false) Boolean active) {
        return ResponseEntity.ok(counterService.list(active));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CounterResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(counterService.getById(id));
    }

    @PostMapping
    public ResponseEntity<CounterResponse> create(@Valid @RequestBody CounterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(counterService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CounterResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CounterRequest request) {
        return ResponseEntity.ok(counterService.update(id, request));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<CounterResponse> activate(@PathVariable Long id) {
        return ResponseEntity.ok(counterService.activate(id));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<CounterResponse> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(counterService.deactivate(id));
    }
}
