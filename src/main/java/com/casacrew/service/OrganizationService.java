package com.casacrew.service;

import com.casacrew.dto.LoginResponseDTO;
import com.casacrew.dto.OrganizationRegistrationRequestDTO;
import com.casacrew.dto.UserResponseDTO;
import com.casacrew.model.Organization;
import com.casacrew.repository.OrganizationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional
public class OrganizationService {

    private static final Logger log = LoggerFactory.getLogger(OrganizationService.class);

    private final OrganizationRepository organizationRepository;
    private final UserService userService;
    private final AuthSessionService authSessionService;

    public OrganizationService(OrganizationRepository organizationRepository,
                               UserService userService,
                               AuthSessionService authSessionService) {
        this.organizationRepository = organizationRepository;
        this.userService = userService;
        this.authSessionService = authSessionService;
    }

    public LoginResponseDTO registerNewOrganization(OrganizationRegistrationRequestDTO request) {
        String organizationName = request.organizationName().trim();

        Organization organization = new Organization(organizationName, generateUniqueSlug(organizationName));
        organization = organizationRepository.save(organization);

        UserResponseDTO admin = userService.createFirstAdminForNewOrganization(
                request.adminUsername().trim(),
                request.adminEmail().trim(),
                request.adminPassword(),
                organization
        );

        AuthSessionService.SessionResult session = authSessionService.createSession(admin.email());

        log.info("Nieuwe organisatie geregistreerd (organizationId={}, slug={}, adminEmail={})",
                organization.getId(), organization.getSlug(), safe(admin.email()));

        return new LoginResponseDTO(
                admin.username(),
                admin.email(),
                "ROLE_ADMIN",
                session.token(),
                session.expiresAt(),
                admin
        );
    }

    private String generateUniqueSlug(String organizationName) {
        String base = organizationName.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");

        if (base.isBlank()) {
            base = "organisatie";
        }
        if (base.length() > 50) {
            base = base.substring(0, 50).replaceAll("-+$", "");
        }

        String candidate = base;
        int suffix = 2;
        while (organizationRepository.existsBySlugIgnoreCase(candidate)) {
            candidate = base + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
