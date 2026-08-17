package com.casacrew.controller;

import com.casacrew.dto.SupplyReportResponseDTO;
import com.casacrew.model.SupplyReport;
import com.casacrew.model.User;
import com.casacrew.repository.SupplyReportRepository;
import com.casacrew.repository.UserRepository;
import com.casacrew.service.UserService;
import com.casacrew.service.WhatsAppService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/supply-reports", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class SupplyReportsController {

    private final SupplyReportRepository supplyReportRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final WhatsAppService whatsAppService;

    public SupplyReportsController(SupplyReportRepository supplyReportRepository,
                                   UserRepository userRepository,
                                   UserService userService,
                                   WhatsAppService whatsAppService) {
        this.supplyReportRepository = supplyReportRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.whatsAppService = whatsAppService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CLEANER')")
    public ResponseEntity<List<SupplyReportResponseDTO>> getAll(Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        List<SupplyReport> reports;
        if (isAdmin) {
            reports = supplyReportRepository.findByOrganization_IdOrderByReportedAtDesc(userService.currentOrganizationId());
        } else {
            User cleaner = resolveUser(auth.getName());
            reports = supplyReportRepository.findByReportedByOrderByReportedAtDesc(cleaner);
        }
        return ResponseEntity.ok(reports.stream().map(this::toDTO).toList());
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','CLEANER')")
    public ResponseEntity<SupplyReportResponseDTO> create(@RequestBody Map<String, String> body, Authentication auth) {
        String itemName = body.get("itemName");
        if (itemName == null || itemName.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "itemName is verplicht.");

        User reporter = resolveUser(auth.getName());
        SupplyReport report = new SupplyReport();
        report.setReportedBy(reporter);
        report.setOrganization(userService.currentOrganization());
        report.setItemName(itemName.trim());
        report.setNotes(body.get("notes"));
        if (body.get("urgency") != null) {
            try { report.setUrgency(SupplyReport.Urgency.valueOf(body.get("urgency").toUpperCase())); }
            catch (IllegalArgumentException ignored) {}
        }

        String studentEmail = body.get("studentEmail");
        if (studentEmail != null && !studentEmail.isBlank()) {
            Long organizationId = userService.currentOrganizationId();
            userRepository.findByEmailIgnoreCase(studentEmail.trim())
                    .filter(u -> u.getRole() == User.Role.STUDENT && u.getOrganization().getId().equals(organizationId))
                    .ifPresent(report::setStudent);
        }

        return ResponseEntity.ok(toDTO(supplyReportRepository.save(report)));
    }

    @PatchMapping(value = "/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SupplyReportResponseDTO> updateStatus(@PathVariable Long id,
                                                     @RequestBody Map<String, String> body) {
        SupplyReport report = findInCurrentOrganizationOrThrow(id);
        try {
            report.setStatus(SupplyReport.Status.valueOf(body.get("status").toUpperCase()));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ongeldige status.");
        }
        report.setUpdatedAt(Instant.now());
        SupplyReport saved = supplyReportRepository.save(report);

        notifyStudentOfStatusChange(saved);

        return ResponseEntity.ok(toDTO(saved));
    }

    private void notifyStudentOfStatusChange(SupplyReport report) {
        User student = report.getStudent();
        if (student == null) return;

        String phone = student.getPhoneNumber();
        if (phone == null || phone.isBlank()) return;

        String statusLabel = switch (report.getStatus()) {
            case PENDING -> "in behandeling";
            case ORDERED -> "besteld";
            case RECEIVED -> "opgelost";
        };

        String message = String.format(
                "Hallo %s! Update over je melding '%s': status is nu \"%s\".",
                student.getUsername(), report.getItemName(), statusLabel);

        whatsAppService.send(phone, message);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        SupplyReport report = findInCurrentOrganizationOrThrow(id);
        supplyReportRepository.delete(report);
        return ResponseEntity.noContent().build();
    }

    private SupplyReport findInCurrentOrganizationOrThrow(Long id) {
        Long organizationId = userService.currentOrganizationId();
        return supplyReportRepository.findById(id)
                .filter(r -> r.getOrganization().getId().equals(organizationId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rapport niet gevonden."));
    }

    private User resolveUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Gebruiker niet gevonden."));
    }

    private SupplyReportResponseDTO toDTO(SupplyReport report) {
        User student = report.getStudent();
        return new SupplyReportResponseDTO(
                report.getId(),
                report.getItemName(),
                report.getNotes(),
                report.getUrgency().name(),
                report.getStatus().name(),
                report.getReportedAt(),
                report.getUpdatedAt(),
                report.getReportedBy().getUsername(),
                student != null ? student.getUsername() : null,
                student != null ? student.getEmail() : null
        );
    }
}
