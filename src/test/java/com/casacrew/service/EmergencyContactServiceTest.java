package com.casacrew.service;

import com.casacrew.model.EmergencyContact;
import com.casacrew.model.Organization;
import com.casacrew.repository.EmergencyContactRepository;
import com.casacrew.repository.OrganizationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmergencyContactServiceTest {

    private static final Long ORG_ID = 1L;

    @Mock EmergencyContactRepository repo;
    @Mock OrganizationRepository organizationRepository;
    @InjectMocks EmergencyContactService emergencyContactService;

    private Organization makeOrganization() {
        Organization organization = new Organization("CasaCrew", "casacrew");
        ReflectionTestUtils.setField(organization, "id", ORG_ID);
        return organization;
    }


    @Test
    void seedDefaultsForOrganization_noExistingContacts_seedsAllFiveDefaults() {
        Organization organization = makeOrganization();
        when(repo.existsByOrganization_IdAndLabel(eq(ORG_ID), anyString())).thenReturn(false);

        emergencyContactService.seedDefaultsForOrganization(organization);

        verify(repo, times(5)).save(any(EmergencyContact.class));
    }

    @Test
    void seedDefaultsForOrganization_someContactsAlreadyExist_skipsThose() {
        Organization organization = makeOrganization();
        when(repo.existsByOrganization_IdAndLabel(ORG_ID, "Alarmnummer (spoed)")).thenReturn(true);
        when(repo.existsByOrganization_IdAndLabel(eq(ORG_ID), argThat(label -> !"Alarmnummer (spoed)".equals(label))))
                .thenReturn(false);

        emergencyContactService.seedDefaultsForOrganization(organization);

        verify(repo, never()).save(argThat(contact -> "Alarmnummer (spoed)".equals(contact.getLabel())));
        verify(repo, times(4)).save(any(EmergencyContact.class));
    }

    @Test
    void seedDefaultsForOrganization_seedsExpectedPhoneNumbersForKnownLabels() {
        Organization organization = makeOrganization();
        when(repo.existsByOrganization_IdAndLabel(eq(ORG_ID), anyString())).thenReturn(false);

        emergencyContactService.seedDefaultsForOrganization(organization);

        verify(repo).save(argThat(c -> "Alarmnummer (spoed)".equals(c.getLabel()) && "112".equals(c.getPhoneNumber())));
        verify(repo).save(argThat(c -> "Huisarts".equals(c.getLabel()) && "".equals(c.getPhoneNumber())));
    }


    @Test
    void seedDefaults_iteratesOverAllOrganizations() {
        Organization org1 = makeOrganization();
        Organization org2 = new Organization("Other", "other");
        ReflectionTestUtils.setField(org2, "id", 2L);
        when(organizationRepository.findAll()).thenReturn(List.of(org1, org2));
        when(repo.existsByOrganization_IdAndLabel(anyLong(), anyString())).thenReturn(false);

        emergencyContactService.seedDefaults();

        verify(repo, times(10)).save(any(EmergencyContact.class));
    }
}
