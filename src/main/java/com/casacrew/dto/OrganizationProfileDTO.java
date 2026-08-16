package com.casacrew.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrganizationProfileDTO(
        @NotBlank(message = "Naam is verplicht")
        @Size(max = 120, message = "Naam mag maximaal 120 tekens zijn")
        String name,

        @Size(max = 255, message = "Adres mag maximaal 255 tekens zijn")
        String address
) {}
