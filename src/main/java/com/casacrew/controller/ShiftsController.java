package com.casacrew.controller;

import com.casacrew.dto.ShiftResponseDTO;
import com.casacrew.model.Shift;
import com.casacrew.model.User;
import com.casacrew.repository.ShiftRepository;
import com.casacrew.repository.UserRepository;
import com.casacrew.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/shifts", produces = MediaType.APPLICATION_JSON_VALUE)
public class ShiftsController {

    private final ShiftRepository shiftRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public ShiftsController(ShiftRepository shiftRepository, UserRepository userRepository, UserService userService) {
        this.shiftRepository = shiftRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ShiftResponseDTO>> getAll() {
        return ResponseEntity.ok(
                shiftRepository.findByOrganization_IdOrderByShiftDateDescCheckInAtDesc(userService.currentOrganizationId())
                        .stream().map(ShiftResponseDTO::from).toList());
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLEANER')")
    public ResponseEntity<List<ShiftResponseDTO>> getMine(Authentication auth) {
        User cleaner = resolveUser(auth.getName());
        return ResponseEntity.ok(
                shiftRepository.findByCleanerOrderByShiftDateDescCheckInAtDesc(cleaner)
                        .stream().map(ShiftResponseDTO::from).toList());
    }

    @PostMapping("/checkin")
    @PreAuthorize("hasRole('CLEANER')")
    public ResponseEntity<ShiftResponseDTO> checkIn(@RequestBody(required = false) Map<String, String> body,
                                         Authentication auth) {
        User cleaner = resolveUser(auth.getName());
        LocalDate today = LocalDate.now();

        Shift shift = new Shift();
        shift.setCleaner(cleaner);
        shift.setOrganization(userService.currentOrganization());
        shift.setShiftDate(today);
        shift.setCheckInAt(Instant.now());
        if (body != null && body.get("notes") != null) shift.setNotes(body.get("notes"));

        return ResponseEntity.ok(ShiftResponseDTO.from(shiftRepository.save(shift)));
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('CLEANER')")
    public ResponseEntity<ShiftResponseDTO> checkOut(@RequestBody(required = false) Map<String, String> body,
                                          Authentication auth) {
        User cleaner = resolveUser(auth.getName());
        Shift shift = shiftRepository.findActiveByCleanerOrderByCheckInAtDesc(cleaner).stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Geen actieve shift gevonden. Check eerst in."));

        shift.setCheckOutAt(Instant.now());
        if (body != null && body.get("notes") != null) shift.setNotes(body.get("notes"));
        Shift saved = shiftRepository.save(shift);

        return ResponseEntity.ok(new ShiftResponseDTO(
                saved.getId(), cleaner.getUsername(), cleaner.getEmail(),
                saved.getShiftDate(), saved.getCheckInAt(), saved.getCheckOutAt(), saved.getNotes()));
    }

    private User resolveUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Gebruiker niet gevonden."));
    }
}
