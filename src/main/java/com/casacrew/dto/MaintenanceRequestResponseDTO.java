package com.casacrew.dto;

import com.casacrew.model.MaintenanceRequest;

import java.time.Instant;

public record MaintenanceRequestResponseDTO(
        Long id,
        String title,
        String description,
        String location,
        String urgency,
        String status,
        String adminNote,
        String reportedByUsername,
        Instant createdAt,
        Instant updatedAt
) {
    public static MaintenanceRequestResponseDTO from(MaintenanceRequest r) {
        return new MaintenanceRequestResponseDTO(
                r.getId(),
                r.getTitle(),
                r.getDescription(),
                r.getLocation(),
                r.getUrgency(),
                r.getStatus(),
                r.getAdminNote(),
                r.getReportedBy() != null ? r.getReportedBy().getUsername() : null,
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}
