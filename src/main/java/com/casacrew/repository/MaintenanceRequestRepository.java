package com.casacrew.repository;

import com.casacrew.model.MaintenanceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Long> {

    List<MaintenanceRequest> findByOrganization_IdOrderByCreatedAtDesc(Long organizationId);

    List<MaintenanceRequest> findByOrganization_IdAndReportedBy_IdOrderByCreatedAtDesc(Long organizationId, Long reportedById);
}
