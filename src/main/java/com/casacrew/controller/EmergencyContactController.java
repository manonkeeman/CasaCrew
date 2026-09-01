package com.casacrew.controller;

import com.casacrew.model.EmergencyContact;
import com.casacrew.repository.EmergencyContactRepository;
import com.casacrew.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/emergency-contacts", produces = MediaType.APPLICATION_JSON_VALUE)
public class EmergencyContactController {

    private final EmergencyContactRepository emergencyContactRepository;
    private final UserService userService;

    public EmergencyContactController(EmergencyContactRepository emergencyContactRepository, UserService userService) {
        this.emergencyContactRepository = emergencyContactRepository;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT','CLEANER')")
    public ResponseEntity<List<EmergencyContact>> getAll() {
        return ResponseEntity.ok(
                emergencyContactRepository.findByOrganization_IdOrderByOrderIndexAscIdAsc(userService.currentOrganizationId()));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmergencyContact> updatePhoneNumber(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long organizationId = userService.currentOrganizationId();
        Optional<EmergencyContact> found = emergencyContactRepository.findById(id)
                .filter(c -> c.getOrganization().getId().equals(organizationId));

        return found.map(contact -> {
            Object phoneNumber = body.get("phoneNumber");
            contact.setPhoneNumber(phoneNumber != null ? phoneNumber.toString().trim() : null);
            return ResponseEntity.ok(emergencyContactRepository.save(contact));
        }).orElse(ResponseEntity.notFound().build());
    }
}
