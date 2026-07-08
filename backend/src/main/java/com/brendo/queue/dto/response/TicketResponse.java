package com.brendo.queue.dto.response;

import com.brendo.queue.entity.TicketPriority;
import com.brendo.queue.entity.TicketStatus;

import java.time.LocalDateTime;

public record TicketResponse(
    Long id,
    String number,
    TicketStatus status,
    TicketPriority priority,
    Long counterId,
    String counterName,
    LocalDateTime createdAt,
    LocalDateTime calledAt,
    LocalDateTime completedAt
) {
}
