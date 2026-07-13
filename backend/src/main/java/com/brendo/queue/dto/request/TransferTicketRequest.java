package com.brendo.queue.dto.request;

import jakarta.validation.constraints.NotNull;

public record TransferTicketRequest(
    @NotNull
    Long counterId
) {
}
