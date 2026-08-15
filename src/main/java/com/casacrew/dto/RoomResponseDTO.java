package com.casacrew.dto;

public record RoomResponseDTO(
        Long id,
        String name,
        Long occupantId,
        String occupantUsername
) {
}