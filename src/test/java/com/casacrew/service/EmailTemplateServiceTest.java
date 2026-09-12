package com.casacrew.service;

import com.casacrew.model.EmailTemplate;
import com.casacrew.model.EmailTemplate.TemplateType;
import com.casacrew.model.Organization;
import com.casacrew.repository.EmailTemplateRepository;
import com.casacrew.repository.OrganizationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailTemplateServiceTest {

    private static final Long ORG_ID = 1L;

    @Mock EmailTemplateRepository repo;
    @Mock OrganizationRepository organizationRepository;
    @Mock UserService userService;
    @InjectMocks EmailTemplateService emailTemplateService;

    private void stubCurrentOrganizationId() {
        when(userService.currentOrganizationId()).thenReturn(ORG_ID);
    }

    private Organization makeOrganization(long id) {
        Organization organization = new Organization("CasaCrew", "casacrew");
        ReflectionTestUtils.setField(organization, "id", id);
        return organization;
    }


    @Test
    void seedDefaultsForOrganization_seedsAllThreeTypesWhenNoneExist() {
        Organization organization = makeOrganization(ORG_ID);
        when(repo.existsByOrganization_IdAndType(eq(ORG_ID), any())).thenReturn(false);

        emailTemplateService.seedDefaultsForOrganization(organization);

        verify(repo, times(3)).save(any(EmailTemplate.class));
    }

    @Test
    void seedDefaultsForOrganization_skipsTypesThatAlreadyExist() {
        Organization organization = makeOrganization(ORG_ID);
        when(repo.existsByOrganization_IdAndType(ORG_ID, TemplateType.PAYMENT_NEW)).thenReturn(true);
        when(repo.existsByOrganization_IdAndType(ORG_ID, TemplateType.PAYMENT_REMINDER_1)).thenReturn(false);
        when(repo.existsByOrganization_IdAndType(ORG_ID, TemplateType.PAYMENT_REMINDER_2)).thenReturn(false);

        emailTemplateService.seedDefaultsForOrganization(organization);

        verify(repo, times(2)).save(any(EmailTemplate.class));
    }

    @Test
    void seedDefaults_iteratesAllOrganizationsAndSeeds() {
        Organization org1 = makeOrganization(1L);
        Organization org2 = makeOrganization(2L);
        when(organizationRepository.findAll()).thenReturn(List.of(org1, org2));
        when(repo.existsByOrganization_IdAndType(any(), any())).thenReturn(false);

        emailTemplateService.seedDefaults();

        verify(repo, times(6)).save(any(EmailTemplate.class));
    }


    @Test
    void getAll_returnsTemplatesForCurrentOrganization() {
        stubCurrentOrganizationId();
        EmailTemplate t1 = new EmailTemplate(TemplateType.PAYMENT_NEW, "Subject", "Body");
        when(repo.findByOrganization_Id(ORG_ID)).thenReturn(List.of(t1));

        List<EmailTemplate> result = emailTemplateService.getAll();

        assertThat(result).containsExactly(t1);
    }


    @Test
    void getByType_found_returnsTemplate() {
        EmailTemplate template = new EmailTemplate(TemplateType.PAYMENT_REMINDER_1, "Subject", "Body");
        when(repo.findByOrganization_IdAndType(ORG_ID, TemplateType.PAYMENT_REMINDER_1))
                .thenReturn(Optional.of(template));

        EmailTemplate result = emailTemplateService.getByType(ORG_ID, TemplateType.PAYMENT_REMINDER_1);

        assertThat(result).isSameAs(template);
    }

    @Test
    void getByType_notFound_throwsIllegalStateException() {
        when(repo.findByOrganization_IdAndType(ORG_ID, TemplateType.PAYMENT_REMINDER_1))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> emailTemplateService.getByType(ORG_ID, TemplateType.PAYMENT_REMINDER_1));
    }

    @Test
    void getByTypeInCurrentOrganization_delegatesWithCurrentOrgId() {
        stubCurrentOrganizationId();
        EmailTemplate template = new EmailTemplate(TemplateType.OVERDUE, "Subject", "Body");
        when(repo.findByOrganization_IdAndType(ORG_ID, TemplateType.OVERDUE)).thenReturn(Optional.of(template));

        EmailTemplate result = emailTemplateService.getByTypeInCurrentOrganization(TemplateType.OVERDUE);

        assertThat(result).isSameAs(template);
    }


    @Test
    void update_savesTemplateWithNewSubjectAndBody() {
        stubCurrentOrganizationId();
        EmailTemplate template = new EmailTemplate(TemplateType.PAYMENT_NEW, "Old subject", "Old body");
        when(repo.findByOrganization_IdAndType(ORG_ID, TemplateType.PAYMENT_NEW)).thenReturn(Optional.of(template));
        when(repo.save(any(EmailTemplate.class))).thenAnswer(inv -> inv.getArgument(0));

        EmailTemplate result = emailTemplateService.update(TemplateType.PAYMENT_NEW, "New subject", "New body");

        assertThat(result.getSubject()).isEqualTo("New subject");
        assertThat(result.getBody()).isEqualTo("New body");
        verify(repo).save(template);
    }

    @Test
    void update_templateNotFound_throwsIllegalStateException() {
        stubCurrentOrganizationId();
        when(repo.findByOrganization_IdAndType(ORG_ID, TemplateType.MISSED_CLEANING)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> emailTemplateService.update(TemplateType.MISSED_CLEANING, "S", "B"));
        verify(repo, never()).save(any());
    }
}
