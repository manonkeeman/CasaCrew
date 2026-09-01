package com.casacrew.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComplaintCreateDTO(
        @NotBlank(message = "Onderwerp is verplicht")
        @Size(max = 150, message = "Onderwerp mag maximaal 150 tekens zijn")
        String subject,

        @NotBlank(message = "Omschrijving is verplicht")
        @Size(max = 2000, message = "Omschrijving mag maximaal 2000 tekens zijn")
        String description,

        // Alleen vereist wanneer een admin een klacht over een specifieke
        // student indient; door een student ingediende klachten hebben
        // geen target (die gaan altijd naar de beheerder in het algemeen).
        Long targetUserId
) {}
