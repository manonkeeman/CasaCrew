package com.casacrew.repository;

import com.casacrew.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findAllByOrderByIdAsc();

    Optional<Room> findByNameIgnoreCase(String name);

    Optional<Room> findByOccupant_Id(Long occupantId);

    List<Room> findByOccupantIsNullOrderByNameAsc();

    List<Room> findByOrganization_IdOrderByIdAsc(Long organizationId);

    Optional<Room> findByOrganization_IdAndNameIgnoreCase(Long organizationId, String name);

    List<Room> findByOrganization_IdAndOccupantIsNullOrderByNameAsc(Long organizationId);

    boolean existsByOrganization_Id(Long organizationId);
}