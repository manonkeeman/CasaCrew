package com.casacrew.service;

import com.casacrew.model.EmailTemplate;
import com.casacrew.model.EmailTemplate.TemplateType;
import com.casacrew.model.Organization;
import com.casacrew.repository.EmailTemplateRepository;
import com.casacrew.repository.OrganizationRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Zolang er nog geen zelfregistratie is (fase 4), bestaat er precies één
 * organisatie ("casacrew", aangemaakt door de V4-backfill-migratie)
 * en seedt deze service de default templates daarvoor. Zodra fase 2 (deel B)
 * services organization-bewust maakt via UserService.currentOrganizationId(),
 * moet deze hardcoded lookup daarnaar verhuizen.
 */
@Service
@Transactional
public class EmailTemplateService {

    private static final Logger log = LoggerFactory.getLogger(EmailTemplateService.class);
    private static final String DEFAULT_ORGANIZATION_SLUG = "casacrew";

    private final EmailTemplateRepository repo;
    private final OrganizationRepository organizationRepository;
    private final UserService userService;

    public EmailTemplateService(EmailTemplateRepository repo, OrganizationRepository organizationRepository,
                                UserService userService) {
        this.repo = repo;
        this.organizationRepository = organizationRepository;
        this.userService = userService;
    }


    @PostConstruct
    public void seedDefaults() {
        seed(TemplateType.PAYMENT_NEW,
                "Factuur {{maand}} – CasaCrew",
                """
                Beste {{naam}},

                Je factuur voor {{maand}} staat klaar. Het te betalen bedrag is {{bedrag}}.

                Betaal veilig via iDEAL:
                {{betaalLink}}

                De vervaldatum is {{vervaldatum}}. Betaal op tijd om extra kosten te voorkomen.

                Met vriendelijke groet,
                CasaCrew
                """);

        seed(TemplateType.PAYMENT_REMINDER_1,
                "Herinnering: factuur {{maand}} nog niet betaald",
                """
                Beste {{naam}},

                We hebben nog geen betaling ontvangen voor {{maand}} ({{bedrag}}).

                Je kunt alsnog betalen via iDEAL:
                {{betaalLink}}

                Vervaldatum: {{vervaldatum}}.

                Met vriendelijke groet,
                CasaCrew
                """);

        seed(TemplateType.PAYMENT_REMINDER_2,
                "Tweede herinnering: factuur {{maand}} – actie vereist",
                """
                Beste {{naam}},

                Dit is de tweede herinnering voor je openstaande factuur van {{maand}} ({{bedrag}}).

                Betaal zo spoedig mogelijk via iDEAL:
                {{betaalLink}}

                Neem contact op met de beheerder als je vragen hebt.

                Met vriendelijke groet,
                CasaCrew
                """);
    }

    private void seed(TemplateType type, String subject, String body) {
        Organization organization = organizationRepository.findBySlugIgnoreCase(DEFAULT_ORGANIZATION_SLUG)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Standaardorganisatie '" + DEFAULT_ORGANIZATION_SLUG + "' niet gevonden -- draai de Flyway-migraties eerst"));

        if (!repo.existsByOrganization_IdAndType(organization.getId(), type)) {
            EmailTemplate template = new EmailTemplate(type, subject, body);
            template.setOrganization(organization);
            repo.save(template);
            log.info("Seeded default email template for type={}", type);
        }
    }


    public List<EmailTemplate> getAll() {
        return repo.findByOrganization_Id(userService.currentOrganizationId());
    }

    /**
     * Alleen voor de achtergrond-jobs (MonthlyRentInvoiceJob,
     * PaymentReminderJob), die vandaag geen ingelogde gebruiker hebben om de
     * organisatie van af te leiden -- draait daarom nog org-loos. Zolang er
     * maar één organisatie bestaat is dat correct; zodra fase 4 een tweede
     * organisatie toelaat, moeten deze jobs zelf per organisatie itereren en
     * moet dit org-loze pad verdwijnen.
     */
    public EmailTemplate getByType(TemplateType type) {
        return repo.findByType(type)
                .orElseThrow(() -> new IllegalStateException("Email template niet gevonden voor type: " + type));
    }

    public EmailTemplate getByTypeInCurrentOrganization(TemplateType type) {
        return repo.findByOrganization_IdAndType(userService.currentOrganizationId(), type)
                .orElseThrow(() -> new IllegalStateException("Email template niet gevonden voor type: " + type));
    }

    public EmailTemplate update(TemplateType type, String subject, String body) {
        EmailTemplate template = getByTypeInCurrentOrganization(type);
        template.setSubject(subject);
        template.setBody(body);
        log.info("Email template updated for type={}", type);
        return repo.save(template);
    }
}