package com.brendo.queue.dto.response;

import java.time.LocalDate;

public record ReportSummaryResponse(
    LocalDate from,
    LocalDate to,
    long totalTickets,
    long waitingTickets,
    long calledTickets,
    long inServiceTickets,
    long completedTickets,
    long cancelledTickets,
    long averageWaitSeconds,
    long averageServiceSeconds
) {
}
