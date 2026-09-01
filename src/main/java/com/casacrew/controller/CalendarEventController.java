package com.casacrew.controller;

import com.casacrew.dto.CalendarEventRequestDTO;
import com.casacrew.dto.CalendarEventResponseDTO;
import com.casacrew.service.CalendarEventService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/calendar-events", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ADMIN','STUDENT','CLEANER')")
public class CalendarEventController {

    private final CalendarEventService calendarEventService;

    public CalendarEventController(CalendarEventService calendarEventService) {
        this.calendarEventService = calendarEventService;
    }

    @GetMapping
    public ResponseEntity<List<CalendarEventResponseDTO>> getAll() {
        return ResponseEntity.ok(calendarEventService.list());
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CalendarEventResponseDTO> create(@Valid @RequestBody CalendarEventRequestDTO request) {
        return ResponseEntity.ok(calendarEventService.create(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        calendarEventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
