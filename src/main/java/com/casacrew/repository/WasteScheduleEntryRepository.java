package com.casacrew.repository;

import com.casacrew.model.WasteScheduleEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WasteScheduleEntryRepository extends JpaRepository<WasteScheduleEntry, Long> {

    List<WasteScheduleEntry> findByOrganization_IdOrderByOrderIndexAscIdAsc(Long organizationId);

    boolean existsByOrganization_IdAndWasteType(Long organizationId, String wasteType);
}
