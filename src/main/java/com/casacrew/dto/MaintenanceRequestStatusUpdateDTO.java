package com.casacrew.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MaintenanceRequestStatusUpdateDTO(
        @NotBlank(message = "Status is verplicht")
        @Pattern(regexp = "OPEN|IN_PROGRESS|RESOLVED", message = "Status moet OPEN, IN_PROGRESS of RESOLVED zijn")
        String status,

        @Size(max = 2000, message = "Notitie mag maximaal 2000 tekens zijn")
        String adminNote
) {}
