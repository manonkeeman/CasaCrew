package com.casacrew.service;

import com.casacrew.dto.OrganizationPaymentSettingsDTO;
import com.casacrew.dto.OrganizationProfileDTO;
import com.casacrew.dto.OrganizationRegistrationRequestDTO;
import com.casacrew.dto.OrganizationRentSettingsDTO;
import com.casacrew.dto.UserResponseDTO;
import com.casacrew.model.Organization;
import com.casacrew.repository.OrganizationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    private static final Long ORG_ID = 1L;

    @Mock OrganizationRepository organizationRepository;
    @Mock UserService userService;
    @Mock AuthSessionService authSessionService;
    @Mock EmailTemplateService emailTemplateService;
    @Mock EmergencyContactService emergencyContactService;
    @Mock WasteScheduleService wasteScheduleService;
    @InjectMocks OrganizationService organizationService;

    private void stubCurrentOrganizationId() {
        when(userService.currentOrganizationId()).thenReturn(ORG_ID);
    }

    private Organization makeOrganization() {
        Organization organization = new Organization("CasaCrew", "casacrew");
        ReflectionTestUtils.setField(organization, "id", ORG_ID);
        return organization;
    }

    private UserResponseDTO makeAdminDto() {
        return new UserResponseDTO(1L, "admin", null, "admin@test.com", "ADMIN", null, null, null,
                null, null, null, null, null, null, false, null, null, null, null);
    }


    @Test
    void registerNewOrganization_success_returnsLoginResponseWithSession() {
        when(organizationRepository.existsBySlugIgnoreCase(anyString())).thenReturn(false);
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> {
            Organization org = inv.getArgument(0);
            if (org.getId() == null) {
                ReflectionTestUtils.setField(org, "id", ORG_ID);
            }
            return org;
        });
        UserResponseDTO adminDto = makeAdminDto();
        when(userService.createFirstAdminForNewOrganization(eq("Casa Del Sol"), eq("admin@test.com"), eq("password123"), any(Organization.class)))
                .thenReturn(adminDto);
        AuthSessionService.SessionResult session = new AuthSessionService.SessionResult("tok-123", Instant.now());
        when(authSessionService.createSession("admin@test.com")).thenReturn(session);

        OrganizationRegistrationRequestDTO request = new OrganizationRegistrationRequestDTO(
                "Casa Del Sol", "Casa Del Sol", "admin@test.com", "password123");

        var result = organizationService.registerNewOrganization(request);

        assertThat(result.token()).isEqualTo("tok-123");
        assertThat(result.email()).isEqualTo("admin@test.com");
        assertThat(result.role()).isEqualTo("ROLE_ADMIN");

        ArgumentCaptor<Organization> orgCaptor = ArgumentCaptor.forClass(Organization.class);
        verify(organizationRepository, times(2)).save(orgCaptor.capture());
        assertThat(orgCaptor.getValue().getSlug()).isEqualTo("casa-del-sol");
        assertThat(orgCaptor.getValue().getComplaintsPolicy()).isNotBlank();

        verify(emailTemplateService).seedDefaultsForOrganization(any(Organization.class));
        verify(emergencyContactService).seedDefaultsForOrganization(any(Organization.class));
        verify(wasteScheduleService).seedDefaultsForOrganization(any(Organization.class));
    }

    @Test
    void registerNewOrganization_slugCollision_appendsNumericSuffix() {
        when(organizationRepository.existsBySlugIgnoreCase("casa-del-sol")).thenReturn(true);
        when(organizationRepository.existsBySlugIgnoreCase("casa-del-sol-2")).thenReturn(false);
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userService.createFirstAdminForNewOrganization(anyString(), anyString(), anyString(), any(Organization.class)))
                .thenReturn(makeAdminDto());
        when(authSessionService.createSession(anyString()))
                .thenReturn(new AuthSessionService.SessionResult("tok", Instant.now()));

        OrganizationRegistrationRequestDTO request = new OrganizationRegistrationRequestDTO(
                "Casa Del Sol", "Casa Del Sol", "admin@test.com", "password123");

        organizationService.registerNewOrganization(request);

        ArgumentCaptor<Organization> orgCaptor = ArgumentCaptor.forClass(Organization.class);
        verify(organizationRepository, atLeastOnce()).save(orgCaptor.capture());
        assertThat(orgCaptor.getValue().getSlug()).isEqualTo("casa-del-sol-2");
    }


    @Test
    void getPaymentSettings_returnsDto() {
        stubCurrentOrganizationId();
        Organization org = makeOrganization();
        org.setBunqMeUsername("casacrew");
        org.setIban("NL00INGB0001234567");
        org.setAccountHolderName("M. Staal");
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(org));

        OrganizationPaymentSettingsDTO result = organizationService.getPaymentSettings();

        assertThat(result.bunqMeUsername()).isEqualTo("casacrew");
        assertThat(result.iban()).isEqualTo("NL00INGB0001234567");
    }

    @Test
    void getPaymentSettings_organizationNotFound_throwsEntityNotFoundException() {
        stubCurrentOrganizationId();
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> organizationService.getPaymentSettings());
    }

    @Test
    void updatePaymentSettings_success_updatesAndReturnsDto() {
        stubCurrentOrganizationId();
        Organization org = makeOrganization();
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(org));
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));

        OrganizationPaymentSettingsDTO request = new OrganizationPaymentSettingsDTO("newuser", "NL00INGB0009999999", "J. Jansen");
        OrganizationPaymentSettingsDTO result = organizationService.updatePaymentSettings(request);

        assertThat(result.bunqMeUsername()).isEqualTo("newuser");
        assertThat(result.accountHolderName()).isEqualTo("J. Jansen");
    }


    @Test
    void getProfile_returnsDto() {
        stubCurrentOrganizationId();
        Organization org = makeOrganization();
        org.setAddress("Hoofdstraat 1");
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(org));

        OrganizationProfileDTO result = organizationService.getProfile();

        assertThat(result.name()).isEqualTo("CasaCrew");
        assertThat(result.address()).isEqualTo("Hoofdstraat 1");
    }

    @Test
    void updateProfile_success_updatesAndReturnsDto() {
        stubCurrentOrganizationId();
        Organization org = makeOrganization();
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(org));
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));

        OrganizationProfileDTO request = new OrganizationProfileDTO("Nieuwe Naam", "Nieuwe Straat 2");
        OrganizationProfileDTO result = organizationService.updateProfile(request);

        assertThat(result.name()).isEqualTo("Nieuwe Naam");
        assertThat(result.address()).isEqualTo("Nieuwe Straat 2");
    }


    @Test
    void getRentSettings_returnsDto() {
        stubCurrentOrganizationId();
        Organization org = makeOrganization();
        org.setDefaultRentAmount(new BigDecimal("350.00"));
        org.setRentInvoiceDayOfMonth(1);
        org.setRentDueDayOfMonth(28);
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(org));

        OrganizationRentSettingsDTO result = organizationService.getRentSettings();

        assertThat(result.defaultRentAmount()).isEqualByComparingTo("350.00");
        assertThat(result.rentInvoiceDayOfMonth()).isEqualTo(1);
        assertThat(result.rentDueDayOfMonth()).isEqualTo(28);
    }

    @Test
    void updateRentSettings_success_updatesAndReturnsDto() {
        stubCurrentOrganizationId();
        Organization org = makeOrganization();
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(org));
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));

        OrganizationRentSettingsDTO request = new OrganizationRentSettingsDTO(new BigDecimal("400.00"), 2, 27);
        OrganizationRentSettingsDTO result = organizationService.updateRentSettings(request);

        assertThat(result.defaultRentAmount()).isEqualByComparingTo("400.00");
        assertThat(result.rentInvoiceDayOfMonth()).isEqualTo(2);
        assertThat(result.rentDueDayOfMonth()).isEqualTo(27);
    }
}
