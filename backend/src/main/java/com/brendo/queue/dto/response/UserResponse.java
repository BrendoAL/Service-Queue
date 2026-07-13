package com.brendo.queue.dto.response;

import java.time.LocalDateTime;

public record UserResponse(
    Long id,
    String username,
    String role,
    Long counterId,
    LocalDateTime createdAt
) {
}
