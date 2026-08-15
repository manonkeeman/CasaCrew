package com.casacrew.repository;

import com.casacrew.model.SupplyReport;
import com.casacrew.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SupplyReportRepository extends JpaRepository<SupplyReport, Long> {
    List<SupplyReport> findByReportedByOrderByReportedAtDesc(User reportedBy);
    List<SupplyReport> findAllByOrderByReportedAtDesc();

    List<SupplyReport> findByOrganization_IdOrderByReportedAtDesc(Long organizationId);
}
