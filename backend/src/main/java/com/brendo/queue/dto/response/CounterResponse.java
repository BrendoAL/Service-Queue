package com.brendo.queue.dto.response;

import java.time.LocalDateTime;

public record CounterResponse(
    Long id,
    String name,
    boolean active,
    LocalDateTime createdAt
) {
}
