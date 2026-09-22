package com.casacrew.repository;

import com.casacrew.model.Huisregel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HuisregelRepository extends JpaRepository<Huisregel, Long> {
    List<Huisregel> findAllByOrderByOrderIndexAscIdAsc();

    List<Huisregel> findByOrganization_IdOrderByOrderIndexAscIdAsc(Long organizationId);

    boolean existsByOrganization_Id(Long organizationId);
}
