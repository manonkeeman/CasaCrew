package com.casacrew.controller;

import com.casacrew.dto.OrganizationPaymentSettingsDTO;
import com.casacrew.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/admin/organization/payment-settings", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ADMIN')")
public class OrganizationSettingsController {

    private final OrganizationService organizationService;

    public OrganizationSettingsController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping
    public ResponseEntity<OrganizationPaymentSettingsDTO> get() {
        return ResponseEntity.ok(organizationService.getPaymentSettings());
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrganizationPaymentSettingsDTO> update(@Valid @RequestBody OrganizationPaymentSettingsDTO request) {
        return ResponseEntity.ok(organizationService.updatePaymentSettings(request));
    }
}
