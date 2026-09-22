package com.casacrew.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactRequestDTO(
        @NotBlank(message = "Naam is verplicht")
        @Size(max = 100, message = "Naam mag maximaal 100 tekens zijn")
        String name,

        @NotBlank(message = "E-mailadres is verplicht")
        @Email(message = "E-mailadres moet geldig zijn")
        @Size(max = 150, message = "E-mailadres mag maximaal 150 tekens zijn")
        String email,

        @NotBlank(message = "Bericht is verplicht")
        @Size(max = 2000, message = "Bericht mag maximaal 2000 tekens zijn")
        String message
) {}
