package com.brendo.queue.dto.response;

import java.util.List;

public record QueueStatusResponse(
    long waiting,
    long called,
    long inService,
    List<TicketResponse> lastCalled
) {
}
