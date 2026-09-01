package com.casacrew.repository;

import com.casacrew.model.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {

    List<EmergencyContact> findByOrganization_IdOrderByOrderIndexAscIdAsc(Long organizationId);

    boolean existsByOrganization_IdAndLabel(Long organizationId, String label);
}
