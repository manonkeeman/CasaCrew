package com.casacrew.dto;

import com.casacrew.model.Complaint;

import java.time.Instant;

public record ComplaintResponseDTO(
        Long id,
        String direction,
        String authorUsername,
        String targetUsername,
        String subject,
        String description,
        String status,
        String response,
        Instant createdAt,
        Instant updatedAt
) {
    public static ComplaintResponseDTO from(Complaint complaint) {
        return new ComplaintResponseDTO(
                complaint.getId(),
                complaint.getDirection(),
                complaint.getAuthor() != null ? complaint.getAuthor().getUsername() : null,
                complaint.getTarget() != null ? complaint.getTarget().getUsername() : null,
                complaint.getSubject(),
                complaint.getDescription(),
                complaint.getStatus(),
                complaint.getResponse(),
                complaint.getCreatedAt(),
                complaint.getUpdatedAt()
        );
    }
}
