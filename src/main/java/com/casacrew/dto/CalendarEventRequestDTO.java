package com.casacrew.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record CalendarEventRequestDTO(
        @NotBlank(message = "Titel is verplicht")
        @Size(max = 150, message = "Titel mag maximaal 150 tekens zijn")
        String title,

        @Size(max = 1000, message = "Omschrijving mag maximaal 1000 tekens zijn")
        String description,

        @NotNull(message = "Datum is verplicht")
        LocalDate eventDate,

        LocalTime eventTime
) {}
