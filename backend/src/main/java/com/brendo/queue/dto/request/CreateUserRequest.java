package com.brendo.queue.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
    @NotBlank
    @Size(max = 50)
    String username,

    @NotBlank
    @Size(min = 8, max = 100)
    String password,

    @NotBlank
    @Pattern(regexp = "ADMIN|ATENDENTE")
    String role,

    Long counterId
) {
}
