package com.casacrew.controller;

import com.casacrew.dto.AnnouncementResponseDTO;
import com.casacrew.model.Announcement;
import com.casacrew.model.User;
import com.casacrew.repository.AnnouncementRepository;
import com.casacrew.repository.UserRepository;
import com.casacrew.service.PushNotificationService;
import com.casacrew.service.UserService;
import com.casacrew.service.WhatsAppService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping(value = "/api/announcements", produces = MediaType.APPLICATION_JSON_VALUE)
public class AnnouncementController {

    private static final Logger log = LoggerFactory.getLogger(AnnouncementController.class);

    private final AnnouncementRepository announcementRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final PushNotificationService pushNotificationService;
    private final WhatsAppService whatsAppService;

    public AnnouncementController(AnnouncementRepository announcementRepository, UserService userService,
                                  UserRepository userRepository, PushNotificationService pushNotificationService,
                                  WhatsAppService whatsAppService) {
        this.announcementRepository = announcementRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.pushNotificationService = pushNotificationService;
        this.whatsAppService = whatsAppService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<List<AnnouncementResponseDTO>> getAll() {
        List<AnnouncementResponseDTO> result = announcementRepository
                .findByOrganization_IdOrderByCreatedAtDesc(userService.currentOrganizationId())
                .stream()
                .map(this::toDTO)
                .toList();
        return ResponseEntity.ok(result);
    }

    public record CreateRequest(
            @Pattern(regexp = "mededeling|onderhoud|evenement",
                    flags = Pattern.Flag.CASE_INSENSITIVE,
                    message = "type moet mededeling, onderhoud of evenement zijn")
            String type,

            @NotBlank(message = "title is verplicht")
            @Size(max = 120, message = "title mag maximaal 120 tekens zijn")
            String title,

            @NotBlank(message = "body is verplicht")
            @Size(max = 2000, message = "body mag maximaal 2000 tekens zijn")
            String body,

            @Size(max = 80, message = "author mag maximaal 80 tekens zijn")
            String author
    ) {}

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnnouncementResponseDTO> create(@Valid @RequestBody CreateRequest req) {
        Announcement ann = new Announcement();
        ann.setTitle(req.title() != null ? req.title().trim() : "");
        ann.setBody(req.body() != null ? req.body().trim() : "");
        ann.setAuthor(req.author() != null ? req.author().trim() : "Beheerder");
        ann.setCreatedAt(LocalDateTime.now());
        ann.setOrganization(userService.currentOrganization());
        try {
            ann.setType(Announcement.AnnouncementType.valueOf(
                    req.type() != null ? req.type().trim().toLowerCase() : "mededeling"));
        } catch (IllegalArgumentException e) {
            ann.setType(Announcement.AnnouncementType.mededeling);
        }
        Announcement saved = announcementRepository.save(ann);

        List<User> students = userRepository.findByOrganization_IdAndRole(
                userService.currentOrganizationId(), User.Role.STUDENT);

        try {
            pushNotificationService.sendToUsers(students, saved.getTitle(), saved.getBody());
        } catch (Exception e) {
            log.error("Push notification failed for announcement id={}: {}", saved.getId(), e.getMessage());
        }

        try {
            List<String> studentPhones = students.stream()
                    .map(User::getPhoneNumber)
                    .filter(phone -> phone != null && !phone.isBlank())
                    .toList();
            String waMsg = String.format("📢 %s: %s", saved.getTitle(), saved.getBody());
            whatsAppService.sendToAll(studentPhones, waMsg);
        } catch (Exception e) {
            log.error("WhatsApp notification failed for announcement id={}: {}", saved.getId(), e.getMessage());
        }

        return ResponseEntity.ok(toDTO(saved));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        Announcement ann = findInCurrentOrganizationOrThrow(id);
        announcementRepository.delete(ann);
        return ResponseEntity.ok(Map.of("message", "Verwijderd"));
    }

    private Announcement findInCurrentOrganizationOrThrow(Long id) {
        Announcement ann = announcementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Aankondiging niet gevonden: id=" + id));
        if (!ann.getOrganization().getId().equals(userService.currentOrganizationId())) {
            throw new EntityNotFoundException("Aankondiging niet gevonden: id=" + id);
        }
        return ann;
    }

    private AnnouncementResponseDTO toDTO(Announcement a) {
        return new AnnouncementResponseDTO(
                a.getId(),
                a.getType() != null ? a.getType().name() : "mededeling",
                a.getTitle(),
                a.getBody(),
                a.getAuthor(),
                a.getCreatedAt()
        );
    }
}