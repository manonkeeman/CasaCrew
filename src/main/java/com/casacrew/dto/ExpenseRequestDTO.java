package com.casacrew.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequestDTO(
        @NotBlank(message = "Categorie is verplicht")
        @Pattern(regexp = "ONDERHOUD|SCHOONMAAK|REPARATIE|INVENTARIS|NUTSVOORZIENINGEN|OVERIG",
                message = "Ongeldige categorie")
        String category,

        @NotBlank(message = "Omschrijving is verplicht")
        @Size(max = 255, message = "Omschrijving mag maximaal 255 tekens zijn")
        String description,

        @NotNull(message = "Bedrag is verplicht")
        @DecimalMin(value = "0.0", message = "Bedrag mag niet negatief zijn")
        BigDecimal amount,

        @NotNull(message = "Datum is verplicht")
        LocalDate expenseDate
) {}
