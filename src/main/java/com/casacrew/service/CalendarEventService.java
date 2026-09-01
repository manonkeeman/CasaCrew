package com.casacrew.service;

import com.casacrew.dto.CalendarEventRequestDTO;
import com.casacrew.dto.CalendarEventResponseDTO;
import com.casacrew.model.CalendarEvent;
import com.casacrew.model.User;
import com.casacrew.repository.CalendarEventRepository;
import com.casacrew.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CalendarEventService {

    private final CalendarEventRepository repository;
    private final UserRepository userRepository;
    private final UserService userService;

    public CalendarEventService(CalendarEventRepository repository, UserRepository userRepository, UserService userService) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public CalendarEventResponseDTO create(CalendarEventRequestDTO dto) {
        CalendarEvent event = new CalendarEvent();
        event.setOrganization(userService.currentOrganization());
        event.setCreatedBy(userRepository.getReferenceById(userService.getMyId()));
        event.setTitle(dto.title());
        event.setDescription(dto.description());
        event.setEventDate(dto.eventDate());
        event.setEventTime(dto.eventTime());
        return CalendarEventResponseDTO.from(repository.save(event));
    }

    @Transactional(readOnly = true)
    public List<CalendarEventResponseDTO> list() {
        return repository.findByOrganization_IdOrderByEventDateAscEventTimeAsc(userService.currentOrganizationId())
                .stream().map(CalendarEventResponseDTO::from).toList();
    }

    public void delete(Long id) {
        Long organizationId = userService.currentOrganizationId();
        CalendarEvent event = repository.findById(id)
                .filter(e -> e.getOrganization().getId().equals(organizationId))
                .orElseThrow(() -> new EntityNotFoundException("Agenda-item niet gevonden: " + id));

        User me = userRepository.getReferenceById(userService.getMyId());
        boolean isOwner = event.getCreatedBy() != null && event.getCreatedBy().getId().equals(me.getId());
        boolean isAdmin = "ADMIN".equals(userService.getMe().role());
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Je kunt alleen je eigen agenda-items verwijderen.");
        }

        repository.delete(event);
    }
}
