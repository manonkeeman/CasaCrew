package com.casacrew.dto;

import com.casacrew.model.CalendarEvent;

import java.time.LocalDate;
import java.time.LocalTime;

public record CalendarEventResponseDTO(
        Long id,
        String title,
        String description,
        LocalDate eventDate,
        LocalTime eventTime,
        String createdByUsername
) {
    public static CalendarEventResponseDTO from(CalendarEvent e) {
        return new CalendarEventResponseDTO(
                e.getId(),
                e.getTitle(),
                e.getDescription(),
                e.getEventDate(),
                e.getEventTime(),
                e.getCreatedBy() != null ? e.getCreatedBy().getUsername() : null
        );
    }
}
