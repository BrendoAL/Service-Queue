package com.brendo.queue.dto.response;

import java.time.LocalDate;

public record DailyTicketReportResponse(
    LocalDate date,
    long totalTickets,
    long completedTickets,
    long cancelledTickets
) {
}
