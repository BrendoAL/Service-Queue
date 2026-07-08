package com.brendo.queue.dto.request;

import jakarta.validation.constraints.NotNull;

public record CallNextTicketRequest(
    @NotNull
    Long counterId
) {
}
