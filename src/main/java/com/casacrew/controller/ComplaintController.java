package com.casacrew.controller;

import com.casacrew.dto.ComplaintCreateDTO;
import com.casacrew.dto.ComplaintResponseDTO;
import com.casacrew.dto.ComplaintStatusUpdateDTO;
import com.casacrew.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/complaints", produces = MediaType.APPLICATION_JSON_VALUE)
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    public ResponseEntity<ComplaintResponseDTO> create(@Valid @RequestBody ComplaintCreateDTO request, Authentication auth) {
        return ResponseEntity.ok(complaintService.create(auth.getName(), request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ComplaintResponseDTO>> getAll() {
        return ResponseEntity.ok(complaintService.listForOrganization());
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    public ResponseEntity<List<ComplaintResponseDTO>> getMine(Authentication auth) {
        return ResponseEntity.ok(complaintService.listForCurrentUser(auth.getName()));
    }

    @PutMapping(value = "/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComplaintResponseDTO> updateStatus(@PathVariable Long id, @Valid @RequestBody ComplaintStatusUpdateDTO request) {
        return ResponseEntity.ok(complaintService.updateStatus(id, request));
    }

    @GetMapping("/policy")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    public ResponseEntity<Map<String, String>> getPolicy() {
        String policy = complaintService.getPolicy();
        return ResponseEntity.ok(Map.of("policy", policy != null ? policy : ""));
    }

    @PutMapping(value = "/policy", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> updatePolicy(@RequestBody Map<String, String> body) {
        String policy = complaintService.updatePolicy(body.get("policy"));
        return ResponseEntity.ok(Map.of("policy", policy != null ? policy : ""));
    }
}
