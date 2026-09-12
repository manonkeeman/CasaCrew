package com.casacrew.service;

import com.casacrew.dto.ComplaintCreateDTO;
import com.casacrew.dto.ComplaintResponseDTO;
import com.casacrew.dto.ComplaintStatusUpdateDTO;
import com.casacrew.model.Complaint;
import com.casacrew.model.Organization;
import com.casacrew.model.User;
import com.casacrew.repository.ComplaintRepository;
import com.casacrew.repository.OrganizationRepository;
import com.casacrew.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplaintServiceTest {

    private static final Long ORG_ID = 1L;

    @Mock ComplaintRepository complaintRepository;
    @Mock UserRepository userRepository;
    @Mock OrganizationRepository organizationRepository;
    @Mock UserService userService;
    @InjectMocks ComplaintService complaintService;

    private void stubCurrentOrganizationId() {
        when(userService.currentOrganizationId()).thenReturn(ORG_ID);
    }

    private Organization makeOrganization() {
        Organization organization = new Organization("CasaCrew", "casacrew");
        ReflectionTestUtils.setField(organization, "id", ORG_ID);
        return organization;
    }

    private User makeUser(long id, String username, String email, User.Role role) {
        User user = new User(username, email, "hash", role);
        user.setOrganization(makeOrganization());
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Complaint makeComplaint(long id, String direction, User author, User target) {
        Complaint complaint = new Complaint();
        complaint.setOrganization(makeOrganization());
        complaint.setAuthor(author);
        complaint.setTarget(target);
        complaint.setDirection(direction);
        complaint.setSubject("Subject");
        complaint.setDescription("Description");
        ReflectionTestUtils.setField(complaint, "id", id);
        return complaint;
    }


    @Test
    void create_byStudent_setsStudentToAdminDirectionAndNoTarget() {
        User student = makeUser(1L, "student", "s@test.com", User.Role.STUDENT);
        when(userRepository.findByEmailIgnoreCase("s@test.com")).thenReturn(Optional.of(student));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(inv -> {
            Complaint c = inv.getArgument(0);
            ReflectionTestUtils.setField(c, "id", 1L);
            return c;
        });

        ComplaintResponseDTO result = complaintService.create("s@test.com", new ComplaintCreateDTO("Subject", "Description", null));

        assertThat(result.direction()).isEqualTo(Complaint.STUDENT_TO_ADMIN);
        assertThat(result.targetUsername()).isNull();
    }

    @Test
    void create_byAdminWithValidTarget_setsAdminToStudentDirection() {
        User admin = makeUser(1L, "admin", "a@test.com", User.Role.ADMIN);
        User target = makeUser(2L, "student", "s@test.com", User.Role.STUDENT);
        when(userRepository.findByEmailIgnoreCase("a@test.com")).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(inv -> inv.getArgument(0));

        ComplaintResponseDTO result = complaintService.create("a@test.com", new ComplaintCreateDTO("Subject", "Description", 2L));

        assertThat(result.direction()).isEqualTo(Complaint.ADMIN_TO_STUDENT);
        assertThat(result.targetUsername()).isEqualTo("student");
    }

    @Test
    void create_byAdminWithoutTargetUserId_throwsBadRequest() {
        User admin = makeUser(1L, "admin", "a@test.com", User.Role.ADMIN);
        when(userRepository.findByEmailIgnoreCase("a@test.com")).thenReturn(Optional.of(admin));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> complaintService.create("a@test.com", new ComplaintCreateDTO("Subject", "Description", null)));
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(complaintRepository, never()).save(any());
    }

    @Test
    void create_byAdminWithTargetFromDifferentOrganization_throwsBadRequest() {
        User admin = makeUser(1L, "admin", "a@test.com", User.Role.ADMIN);
        User otherOrgTarget = makeUser(2L, "other", "o@test.com", User.Role.STUDENT);
        Organization otherOrg = new Organization("Other", "other");
        ReflectionTestUtils.setField(otherOrg, "id", 2L);
        otherOrgTarget.setOrganization(otherOrg);

        when(userRepository.findByEmailIgnoreCase("a@test.com")).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(otherOrgTarget));

        assertThrows(ResponseStatusException.class,
                () -> complaintService.create("a@test.com", new ComplaintCreateDTO("Subject", "Description", 2L)));
    }

    @Test
    void create_byAdminWithNonStudentTarget_throwsBadRequest() {
        User admin = makeUser(1L, "admin", "a@test.com", User.Role.ADMIN);
        User cleanerTarget = makeUser(2L, "cleaner", "c@test.com", User.Role.CLEANER);
        when(userRepository.findByEmailIgnoreCase("a@test.com")).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(cleanerTarget));

        assertThrows(ResponseStatusException.class,
                () -> complaintService.create("a@test.com", new ComplaintCreateDTO("Subject", "Description", 2L)));
    }

    @Test
    void create_byCleaner_throwsAccessDenied() {
        User cleaner = makeUser(1L, "cleaner", "c@test.com", User.Role.CLEANER);
        when(userRepository.findByEmailIgnoreCase("c@test.com")).thenReturn(Optional.of(cleaner));

        assertThrows(AccessDeniedException.class,
                () -> complaintService.create("c@test.com", new ComplaintCreateDTO("Subject", "Description", null)));
        verify(complaintRepository, never()).save(any());
    }

    @Test
    void create_authorNotFound_throwsUnauthorized() {
        when(userRepository.findByEmailIgnoreCase("missing@test.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> complaintService.create("missing@test.com", new ComplaintCreateDTO("Subject", "Description", null)));
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    @Test
    void listForOrganization_returnsMappedList() {
        stubCurrentOrganizationId();
        User student = makeUser(1L, "student", "s@test.com", User.Role.STUDENT);
        Complaint c = makeComplaint(1L, Complaint.STUDENT_TO_ADMIN, student, null);
        when(complaintRepository.findByOrganization_IdOrderByCreatedAtDesc(ORG_ID)).thenReturn(List.of(c));

        List<ComplaintResponseDTO> result = complaintService.listForOrganization();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
    }

    @Test
    void listForCurrentUser_returnsComplaintsConcerningUser() {
        User student = makeUser(1L, "student", "s@test.com", User.Role.STUDENT);
        when(userRepository.findByEmailIgnoreCase("s@test.com")).thenReturn(Optional.of(student));
        Complaint c = makeComplaint(1L, Complaint.STUDENT_TO_ADMIN, student, null);
        when(complaintRepository.findConcerningUser(ORG_ID, 1L)).thenReturn(List.of(c));

        List<ComplaintResponseDTO> result = complaintService.listForCurrentUser("s@test.com");

        assertThat(result).hasSize(1);
    }


    @Test
    void updateStatus_found_updatesStatusAndResponse() {
        stubCurrentOrganizationId();
        User student = makeUser(1L, "student", "s@test.com", User.Role.STUDENT);
        Complaint c = makeComplaint(1L, Complaint.STUDENT_TO_ADMIN, student, null);
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(c));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(inv -> inv.getArgument(0));

        ComplaintResponseDTO result = complaintService.updateStatus(1L, new ComplaintStatusUpdateDTO(Complaint.STATUS_RESOLVED, "Opgelost"));

        assertThat(result.status()).isEqualTo(Complaint.STATUS_RESOLVED);
        assertThat(result.response()).isEqualTo("Opgelost");
    }

    @Test
    void updateStatus_differentOrganization_throwsEntityNotFoundException() {
        stubCurrentOrganizationId();
        when(complaintRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> complaintService.updateStatus(1L, new ComplaintStatusUpdateDTO(Complaint.STATUS_RESOLVED, null)));
    }


    @Test
    void getPolicy_returnsOrganizationPolicy() {
        stubCurrentOrganizationId();
        Organization organization = makeOrganization();
        organization.setComplaintsPolicy("Beleidstekst");
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(organization));

        assertThat(complaintService.getPolicy()).isEqualTo("Beleidstekst");
    }

    @Test
    void updatePolicy_savesAndReturnsNewPolicy() {
        stubCurrentOrganizationId();
        Organization organization = makeOrganization();
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(organization));

        String result = complaintService.updatePolicy("Nieuw beleid");

        assertThat(result).isEqualTo("Nieuw beleid");
        verify(organizationRepository).save(organization);
    }

    @Test
    void getPolicy_organizationNotFound_throwsEntityNotFoundException() {
        stubCurrentOrganizationId();
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> complaintService.getPolicy());
    }
}
