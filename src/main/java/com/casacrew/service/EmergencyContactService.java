package com.casacrew.service;

import com.casacrew.model.EmergencyContact;
import com.casacrew.model.Organization;
import com.casacrew.repository.EmergencyContactRepository;
import com.casacrew.repository.OrganizationRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Seedt de vaste lijst noodcontacten voor elke organisatie: bij opstarten
 * voor bestaande organisaties (idempotent -- bestaande contacten worden niet
 * overschreven), en direct bij zelfregistratie voor een nieuwe organisatie
 * via seedDefaultsForOrganization(...). De admin kan achteraf alleen het
 * telefoonnummer per contact bijwerken, niet het label toevoegen/verwijderen.
 */
@Service
@Transactional
public class EmergencyContactService {

    private static final Logger log = LoggerFactory.getLogger(EmergencyContactService.class);

    private static final Map<String, String> DEFAULT_CONTACTS = new LinkedHashMap<>();
    static {
        DEFAULT_CONTACTS.put("Alarmnummer (spoed)", "112");
        DEFAULT_CONTACTS.put("Politie (geen spoed)", "0900-8844");
        DEFAULT_CONTACTS.put("Huisarts", "");
        DEFAULT_CONTACTS.put("Beheerder / verhuurder", "");
        DEFAULT_CONTACTS.put("Storing gas / elektra / water", "0800-9009");
    }

    private final EmergencyContactRepository repo;
    private final OrganizationRepository organizationRepository;

    public EmergencyContactService(EmergencyContactRepository repo, OrganizationRepository organizationRepository) {
        this.repo = repo;
        this.organizationRepository = organizationRepository;
    }

    @PostConstruct
    public void seedDefaults() {
        for (Organization organization : organizationRepository.findAll()) {
            seedDefaultsForOrganization(organization);
        }
    }

    public void seedDefaultsForOrganization(Organization organization) {
        int[] orderIndex = {0};
        DEFAULT_CONTACTS.forEach((label, phoneNumber) -> {
            seed(organization, label, phoneNumber, orderIndex[0]);
            orderIndex[0]++;
        });
    }

    private void seed(Organization organization, String label, String phoneNumber, int orderIndex) {
        if (!repo.existsByOrganization_IdAndLabel(organization.getId(), label)) {
            EmergencyContact contact = new EmergencyContact(label, phoneNumber, orderIndex);
            contact.setOrganization(organization);
            repo.save(contact);
            log.info("Seeded default emergency contact (organizationId={}, label={})", organization.getId(), label);
        }
    }
}
