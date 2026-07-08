package com.brendo.queue.dto.response;

public record LoginResponse(
    String token,
    String tokenType,
    long expiresInMs,
    String username,
    String role
) {
}
