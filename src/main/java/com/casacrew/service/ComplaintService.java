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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final UserService userService;

    public ComplaintService(ComplaintRepository complaintRepository, UserRepository userRepository,
                             OrganizationRepository organizationRepository, UserService userService) {
        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.userService = userService;
    }

    public ComplaintResponseDTO create(String authorEmail, ComplaintCreateDTO dto) {
        User author = resolveUser(authorEmail);
        Long organizationId = author.getOrganization().getId();

        Complaint complaint = new Complaint();
        complaint.setOrganization(author.getOrganization());
        complaint.setAuthor(author);
        complaint.setSubject(dto.subject());
        complaint.setDescription(dto.description());

        if (author.getRole() == User.Role.ADMIN) {
            if (dto.targetUserId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "targetUserId is verplicht voor een klacht van beheerder naar student.");
            }
            User target = userRepository.findById(dto.targetUserId())
                    .filter(u -> u.getOrganization().getId().equals(organizationId))
                    .filter(u -> u.getRole() == User.Role.STUDENT)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ongeldige student voor deze klacht."));
            complaint.setDirection(Complaint.ADMIN_TO_STUDENT);
            complaint.setTarget(target);
        } else if (author.getRole() == User.Role.STUDENT) {
            complaint.setDirection(Complaint.STUDENT_TO_ADMIN);
        } else {
            throw new AccessDeniedException("Alleen studenten en beheerders kunnen klachten indienen.");
        }

        return ComplaintResponseDTO.from(complaintRepository.save(complaint));
    }

    @Transactional(readOnly = true)
    public List<ComplaintResponseDTO> listForOrganization() {
        return complaintRepository.findByOrganization_IdOrderByCreatedAtDesc(userService.currentOrganizationId())
                .stream().map(ComplaintResponseDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ComplaintResponseDTO> listForCurrentUser(String email) {
        User user = resolveUser(email);
        return complaintRepository.findConcerningUser(user.getOrganization().getId(), user.getId())
                .stream().map(ComplaintResponseDTO::from).toList();
    }

    public ComplaintResponseDTO updateStatus(Long id, ComplaintStatusUpdateDTO dto) {
        Long organizationId = userService.currentOrganizationId();
        Complaint complaint = complaintRepository.findById(id)
                .filter(c -> c.getOrganization().getId().equals(organizationId))
                .orElseThrow(() -> new EntityNotFoundException("Klacht niet gevonden: " + id));

        complaint.setStatus(dto.status());
        if (dto.response() != null) {
            complaint.setResponse(dto.response());
        }
        complaint.setUpdatedAt(Instant.now());

        return ComplaintResponseDTO.from(complaintRepository.save(complaint));
    }

    @Transactional(readOnly = true)
    public String getPolicy() {
        return currentOrganization().getComplaintsPolicy();
    }

    public String updatePolicy(String policy) {
        Organization organization = currentOrganization();
        organization.setComplaintsPolicy(policy);
        organizationRepository.save(organization);
        return organization.getComplaintsPolicy();
    }

    private Organization currentOrganization() {
        Long organizationId = userService.currentOrganizationId();
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organisatie niet gevonden: " + organizationId));
    }

    private User resolveUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Gebruiker niet gevonden."));
    }
}
