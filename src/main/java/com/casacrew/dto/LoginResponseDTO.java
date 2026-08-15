package com.casacrew.dto;

import java.time.Instant;

public record LoginResponseDTO(
        String username,
        String email,
        String role,
        String token,
        Instant expiresAt,
        UserResponseDTO user
) {
}