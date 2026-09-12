package com.casacrew.service;

import com.casacrew.model.Organization;
import com.casacrew.model.WasteScheduleEntry;
import com.casacrew.repository.OrganizationRepository;
import com.casacrew.repository.WasteScheduleEntryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WasteScheduleServiceTest {

    private static final Long ORG_ID = 1L;

    @Mock WasteScheduleEntryRepository repo;
    @Mock OrganizationRepository organizationRepository;
    @InjectMocks WasteScheduleService wasteScheduleService;

    private Organization makeOrganization() {
        Organization organization = new Organization("CasaCrew", "casacrew");
        ReflectionTestUtils.setField(organization, "id", ORG_ID);
        return organization;
    }


    @Test
    void seedDefaultsForOrganization_noExistingEntries_seedsAllFourDefaults() {
        Organization organization = makeOrganization();
        when(repo.existsByOrganization_IdAndWasteType(eq(ORG_ID), anyString())).thenReturn(false);

        wasteScheduleService.seedDefaultsForOrganization(organization);

        verify(repo, times(4)).save(any(WasteScheduleEntry.class));
    }

    @Test
    void seedDefaultsForOrganization_someEntriesAlreadyExist_skipsThose() {
        Organization organization = makeOrganization();
        when(repo.existsByOrganization_IdAndWasteType(ORG_ID, "Restafval")).thenReturn(true);
        when(repo.existsByOrganization_IdAndWasteType(eq(ORG_ID), argThat(t -> !"Restafval".equals(t)))).thenReturn(false);

        wasteScheduleService.seedDefaultsForOrganization(organization);

        verify(repo, never()).save(argThat(entry -> "Restafval".equals(entry.getWasteType())));
        verify(repo, times(3)).save(any(WasteScheduleEntry.class));
    }

    @Test
    void seedDefaultsForOrganization_nullDepositReturnPolicy_setsDefaultAndSaves() {
        Organization organization = makeOrganization();
        when(repo.existsByOrganization_IdAndWasteType(eq(ORG_ID), anyString())).thenReturn(true);

        wasteScheduleService.seedDefaultsForOrganization(organization);

        assertThat(organization.getDepositReturnPolicy()).isNotBlank();
        verify(organizationRepository).save(organization);
    }

    @Test
    void seedDefaultsForOrganization_existingDepositReturnPolicy_doesNotOverwriteOrSave() {
        Organization organization = makeOrganization();
        organization.setDepositReturnPolicy("Bestaand beleid");
        when(repo.existsByOrganization_IdAndWasteType(eq(ORG_ID), anyString())).thenReturn(true);

        wasteScheduleService.seedDefaultsForOrganization(organization);

        assertThat(organization.getDepositReturnPolicy()).isEqualTo("Bestaand beleid");
        verify(organizationRepository, never()).save(any());
    }


    @Test
    void seedDefaults_iteratesOverAllOrganizations() {
        Organization org1 = makeOrganization();
        Organization org2 = new Organization("Other", "other");
        ReflectionTestUtils.setField(org2, "id", 2L);
        org2.setDepositReturnPolicy("Al ingesteld");
        when(organizationRepository.findAll()).thenReturn(List.of(org1, org2));
        when(repo.existsByOrganization_IdAndWasteType(anyLong(), anyString())).thenReturn(true);

        wasteScheduleService.seedDefaults();

        verify(repo, times(0)).save(any());
        verify(organizationRepository).save(org1);
        verify(organizationRepository, never()).save(org2);
    }
}
