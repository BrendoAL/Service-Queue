package com.brendo.queue.dto.response;

public record CounterReportResponse(
    Long counterId,
    String counterName,
    long calledTickets,
    long completedTickets,
    long averageServiceSeconds
) {
}
