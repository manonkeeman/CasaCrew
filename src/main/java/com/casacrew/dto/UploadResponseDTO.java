package com.casacrew.dto;

public record UploadResponseDTO(
        Long documentId,
        String title,
        String downloadUrl
) {
}