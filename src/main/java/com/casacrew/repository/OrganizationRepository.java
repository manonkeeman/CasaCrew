package com.casacrew.repository;

import com.casacrew.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findBySlugIgnoreCase(String slug);
    boolean existsBySlugIgnoreCase(String slug);
}
