package com.casacrew.service;

import com.casacrew.model.Organization;
import com.casacrew.model.WasteScheduleEntry;
import com.casacrew.repository.OrganizationRepository;
import com.casacrew.repository.WasteScheduleEntryRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Seedt de vaste afvalcategorieën (restafval, GFT+E, PMD, papier) voor elke
 * organisatie -- zelfde patroon als EmergencyContactService. De exacte
 * ophaaldag/frequentie is adresafhankelijk (gemeente/RMN) en wordt daarom
 * bewust leeg geseed; de admin vult die zelf in vanaf de eigen afvalkalender.
 */
@Service
@Transactional
public class WasteScheduleService {

    private static final Logger log = LoggerFactory.getLogger(WasteScheduleService.class);

    private static final Map<String, String> DEFAULT_ENTRIES = new LinkedHashMap<>();
    static {
        DEFAULT_ENTRIES.put("Restafval", "");
        DEFAULT_ENTRIES.put("GFT+E (groenbak)", "");
        DEFAULT_ENTRIES.put("PMD (plastic/metaal/drinkpak)", "");
        DEFAULT_ENTRIES.put("Papier", "");
    }

    private final WasteScheduleEntryRepository repo;
    private final OrganizationRepository organizationRepository;

    public WasteScheduleService(WasteScheduleEntryRepository repo, OrganizationRepository organizationRepository) {
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
        DEFAULT_ENTRIES.forEach((wasteType, scheduleInfo) -> {
            seed(organization, wasteType, scheduleInfo, orderIndex[0]);
            orderIndex[0]++;
        });

        if (organization.getDepositReturnPolicy() == null) {
            organization.setDepositReturnPolicy(
                    "Statiegeldflessen en -blikjes horen niet bij het restafval. Verzamel ze in de daarvoor "
                            + "bestemde bak in de keuken/berging en breng ze zelf terug naar de supermarkt -- "
                            + "spreek eventueel met huisgenoten af wie dit per week doet.");
            organizationRepository.save(organization);
        }
    }

    private void seed(Organization organization, String wasteType, String scheduleInfo, int orderIndex) {
        if (!repo.existsByOrganization_IdAndWasteType(organization.getId(), wasteType)) {
            WasteScheduleEntry entry = new WasteScheduleEntry(wasteType, scheduleInfo, orderIndex);
            entry.setOrganization(organization);
            repo.save(entry);
            log.info("Seeded default waste schedule entry (organizationId={}, wasteType={})", organization.getId(), wasteType);
        }
    }
}
