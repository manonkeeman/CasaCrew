package com.casacrew.service;

import com.casacrew.dto.RoomResponseDTO;
import com.casacrew.model.Room;
import com.casacrew.model.User;
import com.casacrew.repository.RoomRepository;
import com.casacrew.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public RoomService(RoomRepository roomRepository, UserRepository userRepository, UserService userService) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }


    @Transactional(readOnly = true)
    public List<RoomResponseDTO> getAllRoomsDTO() {
        return roomRepository.findByOrganization_IdOrderByIdAsc(userService.currentOrganizationId())
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<RoomResponseDTO> getRoomByIdDTO(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Room id is required");
        }
        return findInCurrentOrganization(id).map(this::toDTO);
    }


    public RoomResponseDTO assignOccupantDTO(Long roomId, Long userId) {
        Room room = findInCurrentOrganization(requireId(roomId, "roomId"))
                .orElseThrow(() -> new EntityNotFoundException("Room not found: " + roomId));

        User user = userRepository.findById(requireId(userId, "userId"))
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        if (room.getOccupant() != null && !room.getOccupant().getId().equals(user.getId())) {
            throw new IllegalStateException("Room is already occupied");
        }

        roomRepository.findByOccupant_Id(user.getId()).ifPresent(existingRoom -> {
            if (!existingRoom.getId().equals(room.getId())) {
                throw new IllegalStateException("User is already assigned to another room");
            }
        });

        room.assignOccupant(user);
        return toDTO(room);
    }

    public RoomResponseDTO removeOccupantDTO(Long roomId) {
        Room room = findInCurrentOrganization(requireId(roomId, "roomId"))
                .orElseThrow(() -> new EntityNotFoundException("Room not found: " + roomId));

        room.removeOccupant();
        return toDTO(room);
    }


    public RoomResponseDTO createRoom(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Naam is verplicht");
        }
        String trimmed = name.trim();
        Long organizationId = userService.currentOrganizationId();
        if (roomRepository.findByOrganization_IdAndNameIgnoreCase(organizationId, trimmed).isPresent()) {
            throw new IllegalStateException("Kamer met naam '" + trimmed + "' bestaat al");
        }
        Room room = new Room(trimmed);
        room.setOrganization(userService.currentOrganization());
        return toDTO(roomRepository.save(room));
    }

    public void deleteRoom(Long id) {
        Room room = findInCurrentOrganization(requireId(id, "id"))
                .orElseThrow(() -> new EntityNotFoundException("Room not found: " + id));
        roomRepository.delete(room);
    }

    private Optional<Room> findInCurrentOrganization(Long id) {
        Long organizationId = userService.currentOrganizationId();
        return roomRepository.findById(id)
                .filter(r -> r.getOrganization().getId().equals(organizationId));
    }


    private RoomResponseDTO toDTO(Room room) {
        Long occupantId = room.getOccupant() != null ? room.getOccupant().getId() : null;
        String occupantUsername = room.getOccupant() != null ? room.getOccupant().getUsername() : null;

        return new RoomResponseDTO(
                room.getId(),
                room.getName(),
                occupantId,
                occupantUsername
        );
    }


    private Long requireId(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value;
    }
}