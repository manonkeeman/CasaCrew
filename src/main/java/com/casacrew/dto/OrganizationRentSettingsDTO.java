package com.casacrew.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

public record OrganizationRentSettingsDTO(
        @DecimalMin(value = "0.01", message = "Huurbedrag moet groter zijn dan 0")
        BigDecimal defaultRentAmount,

        @Min(value = 1, message = "Factuurdag moet tussen 1 en 28 liggen")
        @Max(value = 28, message = "Factuurdag moet tussen 1 en 28 liggen")
        Integer rentInvoiceDayOfMonth,

        @Min(value = 1, message = "Vervaldag moet tussen 1 en 28 liggen")
        @Max(value = 28, message = "Vervaldag moet tussen 1 en 28 liggen")
        Integer rentDueDayOfMonth
) {}
