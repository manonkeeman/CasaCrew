package com.casacrew.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrganizationRegistrationRequestDTO(
        @NotBlank(message = "Organisatienaam is verplicht")
        @Size(max = 120, message = "Organisatienaam mag maximaal 120 tekens zijn")
        String organizationName,

        @NotBlank(message = "Naam is verplicht")
        @Size(min = 2, max = 50, message = "Naam moet tussen 2 en 50 tekens zijn")
        String adminUsername,

        @NotBlank(message = "E-mail is verplicht")
        @Email(message = "E-mail moet geldig zijn")
        @Size(max = 100, message = "E-mail mag maximaal 100 tekens zijn")
        String adminEmail,

        @NotBlank(message = "Wachtwoord is verplicht")
        @Size(min = 8, max = 72, message = "Wachtwoord moet minimaal 8 tekens zijn")
        String adminPassword
) {}
