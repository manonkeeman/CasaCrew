package com.casacrew.controller;

import com.casacrew.model.Organization;
import com.casacrew.model.WasteScheduleEntry;
import com.casacrew.repository.OrganizationRepository;
import com.casacrew.repository.WasteScheduleEntryRepository;
import com.casacrew.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/waste-schedule", produces = MediaType.APPLICATION_JSON_VALUE)
public class WasteScheduleController {

    private final WasteScheduleEntryRepository wasteScheduleEntryRepository;
    private final OrganizationRepository organizationRepository;
    private final UserService userService;

    public WasteScheduleController(WasteScheduleEntryRepository wasteScheduleEntryRepository,
                                    OrganizationRepository organizationRepository,
                                    UserService userService) {
        this.wasteScheduleEntryRepository = wasteScheduleEntryRepository;
        this.organizationRepository = organizationRepository;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT','CLEANER')")
    public ResponseEntity<List<WasteScheduleEntry>> getAll() {
        return ResponseEntity.ok(
                wasteScheduleEntryRepository.findByOrganization_IdOrderByOrderIndexAscIdAsc(userService.currentOrganizationId()));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WasteScheduleEntry> updateScheduleInfo(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long organizationId = userService.currentOrganizationId();
        Optional<WasteScheduleEntry> found = wasteScheduleEntryRepository.findById(id)
                .filter(e -> e.getOrganization().getId().equals(organizationId));

        return found.map(entry -> {
            Object scheduleInfo = body.get("scheduleInfo");
            entry.setScheduleInfo(scheduleInfo != null ? scheduleInfo.toString().trim() : null);
            return ResponseEntity.ok(wasteScheduleEntryRepository.save(entry));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/deposit-policy")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT','CLEANER')")
    public ResponseEntity<Map<String, String>> getDepositPolicy() {
        Organization organization = currentOrganization();
        return ResponseEntity.ok(Map.of("policy", organization.getDepositReturnPolicy() != null ? organization.getDepositReturnPolicy() : ""));
    }

    @PutMapping(value = "/deposit-policy", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> updateDepositPolicy(@RequestBody Map<String, String> body) {
        Organization organization = currentOrganization();
        organization.setDepositReturnPolicy(body.get("policy"));
        organizationRepository.save(organization);
        return ResponseEntity.ok(Map.of("policy", organization.getDepositReturnPolicy() != null ? organization.getDepositReturnPolicy() : ""));
    }

    private Organization currentOrganization() {
        Long organizationId = userService.currentOrganizationId();
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organisatie niet gevonden: " + organizationId));
    }
}
