package com.casacrew.controller;

import com.casacrew.dto.OrganizationPaymentSettingsDTO;
import com.casacrew.dto.OrganizationProfileDTO;
import com.casacrew.dto.OrganizationRentSettingsDTO;
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
@RequestMapping(value = "/api/admin/organization", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ADMIN')")
public class OrganizationSettingsController {

    private final OrganizationService organizationService;

    public OrganizationSettingsController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping("/profile")
    public ResponseEntity<OrganizationProfileDTO> getProfile() {
        return ResponseEntity.ok(organizationService.getProfile());
    }

    @PutMapping(value = "/profile", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrganizationProfileDTO> updateProfile(@Valid @RequestBody OrganizationProfileDTO request) {
        return ResponseEntity.ok(organizationService.updateProfile(request));
    }

    @GetMapping("/payment-settings")
    public ResponseEntity<OrganizationPaymentSettingsDTO> getPaymentSettings() {
        return ResponseEntity.ok(organizationService.getPaymentSettings());
    }

    @PutMapping(value = "/payment-settings", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrganizationPaymentSettingsDTO> updatePaymentSettings(@Valid @RequestBody OrganizationPaymentSettingsDTO request) {
        return ResponseEntity.ok(organizationService.updatePaymentSettings(request));
    }

    @GetMapping("/rent-settings")
    public ResponseEntity<OrganizationRentSettingsDTO> getRentSettings() {
        return ResponseEntity.ok(organizationService.getRentSettings());
    }

    @PutMapping(value = "/rent-settings", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrganizationRentSettingsDTO> updateRentSettings(@Valid @RequestBody OrganizationRentSettingsDTO request) {
        return ResponseEntity.ok(organizationService.updateRentSettings(request));
    }
}
