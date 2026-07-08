package com.brendo.queue.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CounterRequest(
    @NotBlank
    @Size(max = 50)
    String name
) {
}
