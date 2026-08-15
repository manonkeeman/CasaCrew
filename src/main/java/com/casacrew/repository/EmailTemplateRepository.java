package com.casacrew.repository;

import com.casacrew.model.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {

    Optional<EmailTemplate> findByType(EmailTemplate.TemplateType type);

    boolean existsByType(EmailTemplate.TemplateType type);

    Optional<EmailTemplate> findByOrganization_IdAndType(Long organizationId, EmailTemplate.TemplateType type);

    boolean existsByOrganization_IdAndType(Long organizationId, EmailTemplate.TemplateType type);

    List<EmailTemplate> findByOrganization_Id(Long organizationId);
}