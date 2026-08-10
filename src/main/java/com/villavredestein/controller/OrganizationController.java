package com.villavredestein.controller;

import com.villavredestein.dto.LoginResponseDTO;
import com.villavredestein.dto.OrganizationRegistrationRequestDTO;
import com.villavredestein.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(value = "/api/organizations", produces = MediaType.APPLICATION_JSON_VALUE)
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponseDTO> register(@Valid @RequestBody OrganizationRegistrationRequestDTO request) {
        LoginResponseDTO response = organizationService.registerNewOrganization(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
