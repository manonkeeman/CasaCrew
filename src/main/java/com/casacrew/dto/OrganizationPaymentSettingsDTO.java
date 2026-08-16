package com.casacrew.dto;

import jakarta.validation.constraints.Size;

public record OrganizationPaymentSettingsDTO(
        @Size(max = 80, message = "Bunq.me-gebruikersnaam mag maximaal 80 tekens zijn")
        String bunqMeUsername,

        @Size(max = 34, message = "IBAN mag maximaal 34 tekens zijn")
        String iban,

        @Size(max = 120, message = "Naam rekeninghouder mag maximaal 120 tekens zijn")
        String accountHolderName
) {}
