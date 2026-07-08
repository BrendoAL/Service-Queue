package com.brendo.queue.dto.request;

import com.brendo.queue.entity.TicketPriority;

public record CreateTicketRequest(
    TicketPriority priority
) {
}
