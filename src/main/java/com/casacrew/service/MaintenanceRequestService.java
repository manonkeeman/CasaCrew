package com.casacrew.service;

import com.casacrew.dto.MaintenanceRequestCreateDTO;
import com.casacrew.dto.MaintenanceRequestResponseDTO;
import com.casacrew.dto.MaintenanceRequestStatusUpdateDTO;
import com.casacrew.model.MaintenanceRequest;
import com.casacrew.repository.MaintenanceRequestRepository;
import com.casacrew.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class MaintenanceRequestService {

    private final MaintenanceRequestRepository repository;
    private final UserRepository userRepository;
    private final UserService userService;

    public MaintenanceRequestService(MaintenanceRequestRepository repository, UserRepository userRepository,
                                      UserService userService) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public MaintenanceRequestResponseDTO create(MaintenanceRequestCreateDTO dto) {
        MaintenanceRequest request = new MaintenanceRequest();
        request.setOrganization(userService.currentOrganization());
        request.setReportedBy(userRepository.getReferenceById(userService.getMyId()));
        request.setTitle(dto.title());
        request.setDescription(dto.description());
        request.setLocation(dto.location());
        if (dto.urgency() != null && !dto.urgency().isBlank()) {
            request.setUrgency(dto.urgency());
        }
        return MaintenanceRequestResponseDTO.from(repository.save(request));
    }

    @Transactional(readOnly = true)
    public List<MaintenanceRequestResponseDTO> listForOrganization() {
        return repository.findByOrganization_IdOrderByCreatedAtDesc(userService.currentOrganizationId())
                .stream().map(MaintenanceRequestResponseDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public List<MaintenanceRequestResponseDTO> listForCurrentUser() {
        return repository.findByOrganization_IdAndReportedBy_IdOrderByCreatedAtDesc(
                        userService.currentOrganizationId(), userService.getMyId())
                .stream().map(MaintenanceRequestResponseDTO::from).toList();
    }

    public MaintenanceRequestResponseDTO updateStatus(Long id, MaintenanceRequestStatusUpdateDTO dto) {
        Long organizationId = userService.currentOrganizationId();
        MaintenanceRequest request = repository.findById(id)
                .filter(r -> r.getOrganization().getId().equals(organizationId))
                .orElseThrow(() -> new EntityNotFoundException("Onderhoudsmelding niet gevonden: " + id));

        request.setStatus(dto.status());
        if (dto.adminNote() != null) {
            request.setAdminNote(dto.adminNote());
        }
        request.setUpdatedAt(Instant.now());
        return MaintenanceRequestResponseDTO.from(repository.save(request));
    }
}
