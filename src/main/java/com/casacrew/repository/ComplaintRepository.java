package com.casacrew.repository;

import com.casacrew.model.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findByOrganization_IdOrderByCreatedAtDesc(Long organizationId);

    @Query("SELECT c FROM Complaint c WHERE c.organization.id = :organizationId "
            + "AND (c.author.id = :userId OR c.target.id = :userId) "
            + "ORDER BY c.createdAt DESC")
    List<Complaint> findConcerningUser(@Param("organizationId") Long organizationId, @Param("userId") Long userId);
}
