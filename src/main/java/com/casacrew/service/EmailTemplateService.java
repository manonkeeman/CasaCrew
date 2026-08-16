package com.casacrew.service;

import com.casacrew.model.EmailTemplate;
import com.casacrew.model.EmailTemplate.TemplateType;
import com.casacrew.model.Organization;
import com.casacrew.repository.EmailTemplateRepository;
import com.casacrew.repository.OrganizationRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Seedt de default e-mailtemplates voor elke organisatie: bij opstarten voor
 * bestaande organisaties (idempotent -- bestaande templates worden niet
 * overschreven), en direct bij zelfregistratie voor een nieuwe organisatie
 * via seedDefaultsForOrganization(...).
 */
@Service
@Transactional
public class EmailTemplateService {

    private static final Logger log = LoggerFactory.getLogger(EmailTemplateService.class);

    private static final Map<TemplateType, String[]> DEFAULT_TEMPLATES = Map.of(
            TemplateType.PAYMENT_NEW, new String[]{
                    "Factuur {{maand}} – CasaCrew",
                    """
                    Beste {{naam}},

                    Je factuur voor {{maand}} staat klaar. Het te betalen bedrag is {{bedrag}}.

                    Betaal veilig via iDEAL:
                    {{betaalLink}}

                    De vervaldatum is {{vervaldatum}}. Betaal op tijd om extra kosten te voorkomen.

                    Met vriendelijke groet,
                    CasaCrew
                    """
            },
            TemplateType.PAYMENT_REMINDER_1, new String[]{
                    "Herinnering: factuur {{maand}} nog niet betaald",
                    """
                    Beste {{naam}},

                    We hebben nog geen betaling ontvangen voor {{maand}} ({{bedrag}}).

                    Je kunt alsnog betalen via iDEAL:
                    {{betaalLink}}

                    Vervaldatum: {{vervaldatum}}.

                    Met vriendelijke groet,
                    CasaCrew
                    """
            },
            TemplateType.PAYMENT_REMINDER_2, new String[]{
                    "Tweede herinnering: factuur {{maand}} – actie vereist",
                    """
                    Beste {{naam}},

                    Dit is de tweede herinnering voor je openstaande factuur van {{maand}} ({{bedrag}}).

                    Betaal zo spoedig mogelijk via iDEAL:
                    {{betaalLink}}

                    Neem contact op met de beheerder als je vragen hebt.

                    Met vriendelijke groet,
                    CasaCrew
                    """
            }
    );

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
        for (Organization organization : organizationRepository.findAll()) {
            seedDefaultsForOrganization(organization);
        }
    }

    public void seedDefaultsForOrganization(Organization organization) {
        DEFAULT_TEMPLATES.forEach((type, subjectAndBody) -> seed(organization, type, subjectAndBody[0], subjectAndBody[1]));
    }

    private void seed(Organization organization, TemplateType type, String subject, String body) {
        if (!repo.existsByOrganization_IdAndType(organization.getId(), type)) {
            EmailTemplate template = new EmailTemplate(type, subject, body);
            template.setOrganization(organization);
            repo.save(template);
            log.info("Seeded default email template (organizationId={}, type={})", organization.getId(), type);
        }
    }


    public List<EmailTemplate> getAll() {
        return repo.findByOrganization_Id(userService.currentOrganizationId());
    }

    /**
     * Voor achtergrond-jobs die per organisatie itereren en dus geen
     * ingelogde gebruiker hebben om de organisatie van af te leiden.
     */
    public EmailTemplate getByType(Long organizationId, TemplateType type) {
        return repo.findByOrganization_IdAndType(organizationId, type)
                .orElseThrow(() -> new IllegalStateException("Email template niet gevonden voor type: " + type));
    }

    public EmailTemplate getByTypeInCurrentOrganization(TemplateType type) {
        return getByType(userService.currentOrganizationId(), type);
    }

    public EmailTemplate update(TemplateType type, String subject, String body) {
        EmailTemplate template = getByTypeInCurrentOrganization(type);
        template.setSubject(subject);
        template.setBody(body);
        log.info("Email template updated for type={}", type);
        return repo.save(template);
    }
}