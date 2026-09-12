package com.casacrew.service;

import com.casacrew.dto.MaintenanceRequestCreateDTO;
import com.casacrew.dto.MaintenanceRequestResponseDTO;
import com.casacrew.dto.MaintenanceRequestStatusUpdateDTO;
import com.casacrew.model.MaintenanceRequest;
import com.casacrew.model.Organization;
import com.casacrew.model.User;
import com.casacrew.repository.MaintenanceRequestRepository;
import com.casacrew.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceRequestServiceTest {

    private static final Long ORG_ID = 1L;

    @Mock MaintenanceRequestRepository repository;
    @Mock UserRepository userRepository;
    @Mock UserService userService;
    @InjectMocks MaintenanceRequestService maintenanceRequestService;

    private Organization makeOrganization() {
        Organization organization = new Organization("CasaCrew", "casacrew");
        ReflectionTestUtils.setField(organization, "id", ORG_ID);
        return organization;
    }

    private User makeUser(long id, String username, String email) {
        User user = new User(username, email, "hash", User.Role.STUDENT);
        user.setOrganization(makeOrganization());
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private MaintenanceRequest makeRequest(long id, User reportedBy) {
        MaintenanceRequest request = new MaintenanceRequest();
        request.setOrganization(makeOrganization());
        request.setReportedBy(reportedBy);
        request.setTitle("Kapotte kraan");
        request.setDescription("Lekt");
        ReflectionTestUtils.setField(request, "id", id);
        return request;
    }


    @Test
    void create_success_savesWithDefaultsAndReturnsDto() {
        User reporter = makeUser(1L, "student", "s@test.com");
        when(userService.currentOrganization()).thenReturn(makeOrganization());
        when(userService.getMyId()).thenReturn(1L);
        when(userRepository.getReferenceById(1L)).thenReturn(reporter);
        when(repository.save(any(MaintenanceRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        MaintenanceRequestResponseDTO result = maintenanceRequestService.create(
                new MaintenanceRequestCreateDTO("Kapotte kraan", "Lekt hard", "Keuken", "HIGH"));

        assertThat(result.title()).isEqualTo("Kapotte kraan");
        assertThat(result.location()).isEqualTo("Keuken");
        assertThat(result.urgency()).isEqualTo("HIGH");
        assertThat(result.status()).isEqualTo(MaintenanceRequest.STATUS_OPEN);
        assertThat(result.reportedByUsername()).isEqualTo("student");
    }

    @Test
    void create_blankUrgency_keepsDefaultUrgency() {
        User reporter = makeUser(1L, "student", "s@test.com");
        when(userService.currentOrganization()).thenReturn(makeOrganization());
        when(userService.getMyId()).thenReturn(1L);
        when(userRepository.getReferenceById(1L)).thenReturn(reporter);
        when(repository.save(any(MaintenanceRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        MaintenanceRequestResponseDTO result = maintenanceRequestService.create(
                new MaintenanceRequestCreateDTO("Titel", "Omschrijving", null, ""));

        assertThat(result.urgency()).isEqualTo("MEDIUM");
    }


    @Test
    void listForOrganization_returnsMappedList() {
        when(userService.currentOrganizationId()).thenReturn(ORG_ID);
        User reporter = makeUser(1L, "student", "s@test.com");
        MaintenanceRequest r = makeRequest(1L, reporter);
        when(repository.findByOrganization_IdOrderByCreatedAtDesc(ORG_ID)).thenReturn(List.of(r));

        List<MaintenanceRequestResponseDTO> result = maintenanceRequestService.listForOrganization();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
    }

    @Test
    void listForCurrentUser_returnsOnlyOwnRequests() {
        when(userService.currentOrganizationId()).thenReturn(ORG_ID);
        when(userService.getMyId()).thenReturn(1L);
        User reporter = makeUser(1L, "student", "s@test.com");
        MaintenanceRequest r = makeRequest(1L, reporter);
        when(repository.findByOrganization_IdAndReportedBy_IdOrderByCreatedAtDesc(ORG_ID, 1L)).thenReturn(List.of(r));

        List<MaintenanceRequestResponseDTO> result = maintenanceRequestService.listForCurrentUser();

        assertThat(result).hasSize(1);
    }


    @Test
    void updateStatus_found_updatesStatusAndAdminNote() {
        when(userService.currentOrganizationId()).thenReturn(ORG_ID);
        User reporter = makeUser(1L, "student", "s@test.com");
        MaintenanceRequest r = makeRequest(1L, reporter);
        when(repository.findById(1L)).thenReturn(Optional.of(r));
        when(repository.save(any(MaintenanceRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        MaintenanceRequestResponseDTO result = maintenanceRequestService.updateStatus(1L,
                new MaintenanceRequestStatusUpdateDTO(MaintenanceRequest.STATUS_RESOLVED, "Gerepareerd"));

        assertThat(result.status()).isEqualTo(MaintenanceRequest.STATUS_RESOLVED);
        assertThat(result.adminNote()).isEqualTo("Gerepareerd");
    }

    @Test
    void updateStatus_notFound_throwsEntityNotFoundException() {
        when(userService.currentOrganizationId()).thenReturn(ORG_ID);
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> maintenanceRequestService.updateStatus(99L, new MaintenanceRequestStatusUpdateDTO(MaintenanceRequest.STATUS_RESOLVED, null)));
    }

    @Test
    void updateStatus_differentOrganization_throwsEntityNotFoundException() {
        when(userService.currentOrganizationId()).thenReturn(2L);
        User reporter = makeUser(1L, "student", "s@test.com");
        MaintenanceRequest r = makeRequest(1L, reporter);
        when(repository.findById(1L)).thenReturn(Optional.of(r));

        assertThrows(EntityNotFoundException.class,
                () -> maintenanceRequestService.updateStatus(1L, new MaintenanceRequestStatusUpdateDTO(MaintenanceRequest.STATUS_IN_PROGRESS, null)));
        verify(repository, never()).save(any());
    }
}
