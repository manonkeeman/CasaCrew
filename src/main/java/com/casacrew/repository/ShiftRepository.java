package com.casacrew.repository;

import com.casacrew.model.Shift;
import com.casacrew.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {

    @Query("SELECT s FROM Shift s JOIN FETCH s.cleaner WHERE s.cleaner = :cleaner " +
            "ORDER BY s.shiftDate DESC, s.checkInAt DESC")
    List<Shift> findByCleanerOrderByShiftDateDescCheckInAtDesc(@Param("cleaner") User cleaner);

    List<Shift> findAllByOrderByShiftDateDescCheckInAtDesc();

    @Query("SELECT s FROM Shift s JOIN FETCH s.cleaner WHERE s.cleaner = :cleaner AND s.checkOutAt IS NULL " +
            "ORDER BY s.checkInAt DESC")
    List<Shift> findActiveByCleanerOrderByCheckInAtDesc(@Param("cleaner") User cleaner);

    boolean existsByCleanerAndShiftDate(User cleaner, LocalDate date);

    @Query("SELECT s FROM Shift s JOIN FETCH s.cleaner WHERE s.organization.id = :organizationId " +
            "ORDER BY s.shiftDate DESC, s.checkInAt DESC")
    List<Shift> findByOrganization_IdOrderByShiftDateDescCheckInAtDesc(@Param("organizationId") Long organizationId);
}
