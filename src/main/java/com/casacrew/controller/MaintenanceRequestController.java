package com.casacrew.controller;

import com.casacrew.dto.MaintenanceRequestCreateDTO;
import com.casacrew.dto.MaintenanceRequestResponseDTO;
import com.casacrew.dto.MaintenanceRequestStatusUpdateDTO;
import com.casacrew.service.MaintenanceRequestService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/maintenance-requests", produces = MediaType.APPLICATION_JSON_VALUE)
public class MaintenanceRequestController {

    private final MaintenanceRequestService service;

    public MaintenanceRequestController(MaintenanceRequestService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('STUDENT','CLEANER')")
    public ResponseEntity<MaintenanceRequestResponseDTO> create(@Valid @RequestBody MaintenanceRequestCreateDTO request) {
        return ResponseEntity.ok(service.create(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MaintenanceRequestResponseDTO>> getAll() {
        return ResponseEntity.ok(service.listForOrganization());
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('STUDENT','CLEANER')")
    public ResponseEntity<List<MaintenanceRequestResponseDTO>> getMine() {
        return ResponseEntity.ok(service.listForCurrentUser());
    }

    @PutMapping(value = "/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MaintenanceRequestResponseDTO> updateStatus(
            @PathVariable Long id, @Valid @RequestBody MaintenanceRequestStatusUpdateDTO request) {
        return ResponseEntity.ok(service.updateStatus(id, request));
    }
}
