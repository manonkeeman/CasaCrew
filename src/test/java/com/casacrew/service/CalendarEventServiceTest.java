package com.casacrew.service;

import com.casacrew.dto.CalendarEventRequestDTO;
import com.casacrew.dto.CalendarEventResponseDTO;
import com.casacrew.dto.UserResponseDTO;
import com.casacrew.model.CalendarEvent;
import com.casacrew.model.Organization;
import com.casacrew.model.User;
import com.casacrew.repository.CalendarEventRepository;
import com.casacrew.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarEventServiceTest {

    private static final Long ORG_ID = 1L;

    @Mock CalendarEventRepository repository;
    @Mock UserRepository userRepository;
    @Mock UserService userService;
    @InjectMocks CalendarEventService calendarEventService;

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

    private UserResponseDTO makeUserResponseDTO(long id, String role) {
        return new UserResponseDTO(id, "user", "User", "u@test.com", role, null, null, null,
                null, null, null, null, null, null, false, null, null, null, null);
    }

    private CalendarEvent makeEvent(long id, User createdBy) {
        CalendarEvent event = new CalendarEvent();
        event.setOrganization(makeOrganization());
        event.setCreatedBy(createdBy);
        event.setTitle("Huisvergadering");
        event.setEventDate(LocalDate.of(2026, 1, 1));
        ReflectionTestUtils.setField(event, "id", id);
        return event;
    }


    @Test
    void create_success_savesEventForCurrentUser() {
        User creator = makeUser(1L, "student", "s@test.com", User.Role.STUDENT);
        when(userService.currentOrganization()).thenReturn(makeOrganization());
        when(userService.getMyId()).thenReturn(1L);
        when(userRepository.getReferenceById(1L)).thenReturn(creator);
        when(repository.save(any(CalendarEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        CalendarEventResponseDTO result = calendarEventService.create(
                new CalendarEventRequestDTO("Huisvergadering", "Maandelijks overleg", LocalDate.of(2026, 3, 1), null));

        assertThat(result.title()).isEqualTo("Huisvergadering");
    }


    @Test
    void list_returnsMappedEventsOrderedByDate() {
        when(userService.currentOrganizationId()).thenReturn(ORG_ID);
        User creator = makeUser(1L, "student", "s@test.com", User.Role.STUDENT);
        CalendarEvent event = makeEvent(1L, creator);
        when(repository.findByOrganization_IdOrderByEventDateAscEventTimeAsc(ORG_ID)).thenReturn(List.of(event));

        List<CalendarEventResponseDTO> result = calendarEventService.list();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Huisvergadering");
    }


    @Test
    void delete_byOwner_deletesEvent() {
        when(userService.currentOrganizationId()).thenReturn(ORG_ID);
        User owner = makeUser(1L, "student", "s@test.com", User.Role.STUDENT);
        CalendarEvent event = makeEvent(1L, owner);
        when(repository.findById(1L)).thenReturn(Optional.of(event));
        when(userService.getMyId()).thenReturn(1L);
        when(userRepository.getReferenceById(1L)).thenReturn(owner);
        when(userService.getMe()).thenReturn(makeUserResponseDTO(1L, "STUDENT"));

        assertDoesNotThrow(() -> calendarEventService.delete(1L));
        verify(repository).delete(event);
    }

    @Test
    void delete_byAdminNotOwner_deletesEvent() {
        when(userService.currentOrganizationId()).thenReturn(ORG_ID);
        User owner = makeUser(1L, "student", "s@test.com", User.Role.STUDENT);
        User admin = makeUser(2L, "admin", "a@test.com", User.Role.ADMIN);
        CalendarEvent event = makeEvent(1L, owner);
        when(repository.findById(1L)).thenReturn(Optional.of(event));
        when(userService.getMyId()).thenReturn(2L);
        when(userRepository.getReferenceById(2L)).thenReturn(admin);
        when(userService.getMe()).thenReturn(makeUserResponseDTO(2L, "ADMIN"));

        assertDoesNotThrow(() -> calendarEventService.delete(1L));
        verify(repository).delete(event);
    }

    @Test
    void delete_byOtherStudent_throwsAccessDeniedException() {
        when(userService.currentOrganizationId()).thenReturn(ORG_ID);
        User owner = makeUser(1L, "student", "s@test.com", User.Role.STUDENT);
        User other = makeUser(2L, "other", "o@test.com", User.Role.STUDENT);
        CalendarEvent event = makeEvent(1L, owner);
        when(repository.findById(1L)).thenReturn(Optional.of(event));
        when(userService.getMyId()).thenReturn(2L);
        when(userRepository.getReferenceById(2L)).thenReturn(other);
        when(userService.getMe()).thenReturn(makeUserResponseDTO(2L, "STUDENT"));

        assertThrows(AccessDeniedException.class, () -> calendarEventService.delete(1L));
        verify(repository, never()).delete(any());
    }

    @Test
    void delete_notFound_throwsEntityNotFoundException() {
        when(userService.currentOrganizationId()).thenReturn(ORG_ID);
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> calendarEventService.delete(99L));
    }

    @Test
    void delete_differentOrganization_throwsEntityNotFoundException() {
        when(userService.currentOrganizationId()).thenReturn(2L);
        User owner = makeUser(1L, "student", "s@test.com", User.Role.STUDENT);
        CalendarEvent event = makeEvent(1L, owner);
        when(repository.findById(1L)).thenReturn(Optional.of(event));

        assertThrows(EntityNotFoundException.class, () -> calendarEventService.delete(1L));
        verify(repository, never()).delete(any());
    }
}
