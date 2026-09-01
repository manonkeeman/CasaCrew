package com.casacrew.dto;

import com.casacrew.model.Shift;

import java.time.Instant;
import java.time.LocalDate;

public record ShiftResponseDTO(
        Long id,
        String cleanerUsername,
        String cleanerEmail,
        LocalDate shiftDate,
        Instant checkInAt,
        Instant checkOutAt,
        String notes
) {
    public static ShiftResponseDTO from(Shift shift) {
        return new ShiftResponseDTO(
                shift.getId(),
                shift.getCleaner() != null ? shift.getCleaner().getUsername() : null,
                shift.getCleaner() != null ? shift.getCleaner().getEmail() : null,
                shift.getShiftDate(),
                shift.getCheckInAt(),
                shift.getCheckOutAt(),
                shift.getNotes()
        );
    }
}
