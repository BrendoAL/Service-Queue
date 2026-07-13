package com.brendo.queue.dto.response;

public record TicketEventResponse(
    String event,
    TicketResponse ticket
) {
}
