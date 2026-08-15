package com.casacrew.repository;

import com.casacrew.model.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findAllByOrderByCreatedAtDesc();

    List<Announcement> findByOrganization_IdOrderByCreatedAtDesc(Long organizationId);
}
