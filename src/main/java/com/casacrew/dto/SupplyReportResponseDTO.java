package com.casacrew.dto;

import java.time.Instant;

public record SupplyReportResponseDTO(
        Long id,
        String itemName,
        String notes,
        String urgency,
        String status,
        Instant reportedAt,
        Instant updatedAt,
        String reportedByUsername,
        String studentUsername,
        String studentEmail
) {}
