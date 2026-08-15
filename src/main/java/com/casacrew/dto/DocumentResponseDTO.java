package com.casacrew.dto;

public record DocumentResponseDTO(
        Long id,
        String title,
        String description,
        String roleAccess,
        String uploadedBy,
        String downloadUrl
) {
}