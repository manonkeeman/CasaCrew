package com.casacrew.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MaintenanceRequestCreateDTO(
        @NotBlank(message = "Titel is verplicht")
        @Size(max = 150, message = "Titel mag maximaal 150 tekens zijn")
        String title,

        @NotBlank(message = "Omschrijving is verplicht")
        @Size(max = 2000, message = "Omschrijving mag maximaal 2000 tekens zijn")
        String description,

        @Size(max = 100, message = "Locatie mag maximaal 100 tekens zijn")
        String location,

        @Pattern(regexp = "LOW|MEDIUM|HIGH", message = "Urgentie moet LOW, MEDIUM of HIGH zijn")
        String urgency
) {}
