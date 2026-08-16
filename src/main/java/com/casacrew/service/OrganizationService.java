package com.casacrew.service;

import com.casacrew.dto.LoginResponseDTO;
import com.casacrew.dto.OrganizationPaymentSettingsDTO;
import com.casacrew.dto.OrganizationProfileDTO;
import com.casacrew.dto.OrganizationRegistrationRequestDTO;
import com.casacrew.dto.OrganizationRentSettingsDTO;
import com.casacrew.dto.UserResponseDTO;
import com.casacrew.model.Organization;
import com.casacrew.repository.OrganizationRepository;
import jakarta.persistence.EntityNotFoundException;
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
    private final EmailTemplateService emailTemplateService;

    public OrganizationService(OrganizationRepository organizationRepository,
                               UserService userService,
                               AuthSessionService authSessionService,
                               EmailTemplateService emailTemplateService) {
        this.organizationRepository = organizationRepository;
        this.userService = userService;
        this.authSessionService = authSessionService;
        this.emailTemplateService = emailTemplateService;
    }

    public LoginResponseDTO registerNewOrganization(OrganizationRegistrationRequestDTO request) {
        String organizationName = request.organizationName().trim();

        Organization organization = new Organization(organizationName, generateUniqueSlug(organizationName));
        organization = organizationRepository.save(organization);
        emailTemplateService.seedDefaultsForOrganization(organization);

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

    public OrganizationPaymentSettingsDTO getPaymentSettings() {
        Organization organization = currentOrganization();
        return toPaymentSettingsDTO(organization);
    }

    public OrganizationPaymentSettingsDTO updatePaymentSettings(OrganizationPaymentSettingsDTO request) {
        Organization organization = currentOrganization();

        organization.setBunqMeUsername(request.bunqMeUsername());
        organization.setIban(request.iban());
        organization.setAccountHolderName(request.accountHolderName());

        organization = organizationRepository.save(organization);

        log.info("Betaalinstellingen bijgewerkt (organizationId={})", organization.getId());

        return toPaymentSettingsDTO(organization);
    }

    public OrganizationProfileDTO getProfile() {
        return toProfileDTO(currentOrganization());
    }

    public OrganizationProfileDTO updateProfile(OrganizationProfileDTO request) {
        Organization organization = currentOrganization();

        organization.setName(request.name());
        organization.setAddress(request.address());

        organization = organizationRepository.save(organization);

        log.info("Organisatieprofiel bijgewerkt (organizationId={})", organization.getId());

        return toProfileDTO(organization);
    }

    public OrganizationRentSettingsDTO getRentSettings() {
        return toRentSettingsDTO(currentOrganization());
    }

    public OrganizationRentSettingsDTO updateRentSettings(OrganizationRentSettingsDTO request) {
        Organization organization = currentOrganization();

        organization.setDefaultRentAmount(request.defaultRentAmount());
        organization.setRentInvoiceDayOfMonth(request.rentInvoiceDayOfMonth());
        organization.setRentDueDayOfMonth(request.rentDueDayOfMonth());

        organization = organizationRepository.save(organization);

        log.info("Huurinstellingen bijgewerkt (organizationId={})", organization.getId());

        return toRentSettingsDTO(organization);
    }

    private OrganizationProfileDTO toProfileDTO(Organization organization) {
        return new OrganizationProfileDTO(organization.getName(), organization.getAddress());
    }

    private OrganizationRentSettingsDTO toRentSettingsDTO(Organization organization) {
        return new OrganizationRentSettingsDTO(
                organization.getDefaultRentAmount(),
                organization.getRentInvoiceDayOfMonth(),
                organization.getRentDueDayOfMonth()
        );
    }

    private Organization currentOrganization() {
        Long organizationId = userService.currentOrganizationId();
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organisatie niet gevonden: " + organizationId));
    }

    private OrganizationPaymentSettingsDTO toPaymentSettingsDTO(Organization organization) {
        return new OrganizationPaymentSettingsDTO(
                organization.getBunqMeUsername(),
                organization.getIban(),
                organization.getAccountHolderName()
        );
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
