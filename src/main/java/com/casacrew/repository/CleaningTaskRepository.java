package com.casacrew.repository;

import com.casacrew.model.CleaningTask;
import com.casacrew.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CleaningTaskRepository extends JpaRepository<CleaningTask, Long> {

    @Query("SELECT t FROM CleaningTask t LEFT JOIN FETCH t.assignedTo WHERE t.weekNumber = :weekNumber ORDER BY t.id ASC")
    List<CleaningTask> findByWeekNumberOrderByIdAsc(@Param("weekNumber") int weekNumber);

    @Query("SELECT t FROM CleaningTask t LEFT JOIN FETCH t.assignedTo ORDER BY t.weekNumber ASC, t.id ASC")
    List<CleaningTask> findAllByOrderByWeekNumberAscIdAsc();

    @Query("SELECT t FROM CleaningTask t LEFT JOIN FETCH t.assignedTo WHERE t.organization.id = :organizationId ORDER BY t.weekNumber ASC, t.id ASC")
    List<CleaningTask> findByOrganization_IdOrderByWeekNumberAscIdAsc(@Param("organizationId") Long organizationId);

    @Query("SELECT t FROM CleaningTask t LEFT JOIN FETCH t.assignedTo WHERE t.organization.id = :organizationId AND t.weekNumber = :weekNumber ORDER BY t.id ASC")
    List<CleaningTask> findByOrganization_IdAndWeekNumberOrderByIdAsc(@Param("organizationId") Long organizationId, @Param("weekNumber") int weekNumber);

    @EntityGraph(attributePaths = "assignedTo")
    List<CleaningTask> findAllByOrderByIdAsc();

    @Query("""
            SELECT t
            FROM CleaningTask t LEFT JOIN FETCH t.assignedTo
            WHERE UPPER(t.roleAccess) = UPPER(:role)
               OR UPPER(t.roleAccess) = 'ROLE_ALL'
               OR UPPER(t.roleAccess) = 'ALL'
            ORDER BY t.weekNumber ASC, t.id ASC
            """)
    List<CleaningTask> findAccessibleForRole(@Param("role") String role);

    @Query("""
            SELECT t
            FROM CleaningTask t LEFT JOIN FETCH t.assignedTo
            WHERE t.organization.id = :organizationId
              AND (
                   UPPER(t.roleAccess) = UPPER(:role)
                OR UPPER(t.roleAccess) = 'ROLE_ALL'
                OR UPPER(t.roleAccess) = 'ALL'
              )
            ORDER BY t.weekNumber ASC, t.id ASC
            """)
    List<CleaningTask> findAccessibleForRoleInOrganization(@Param("organizationId") Long organizationId, @Param("role") String role);

    @Query("""
            SELECT t
            FROM CleaningTask t LEFT JOIN FETCH t.assignedTo
            WHERE t.weekNumber = :weekNumber
              AND (
                   UPPER(t.roleAccess) = UPPER(:role)
                OR UPPER(t.roleAccess) = 'ROLE_ALL'
                OR UPPER(t.roleAccess) = 'ALL'
              )
            ORDER BY t.id ASC
            """)
    List<CleaningTask> findAccessibleForRoleByWeek(@Param("role") String role,
                                                  @Param("weekNumber") int weekNumber);

    @Query("""
            SELECT t
            FROM CleaningTask t LEFT JOIN FETCH t.assignedTo
            WHERE t.organization.id = :organizationId
              AND t.weekNumber = :weekNumber
              AND (
                   UPPER(t.roleAccess) = UPPER(:role)
                OR UPPER(t.roleAccess) = 'ROLE_ALL'
                OR UPPER(t.roleAccess) = 'ALL'
              )
            ORDER BY t.id ASC
            """)
    List<CleaningTask> findAccessibleForRoleByWeekInOrganization(@Param("organizationId") Long organizationId,
                                                  @Param("role") String role,
                                                  @Param("weekNumber") int weekNumber);

    @Query("SELECT t FROM CleaningTask t LEFT JOIN FETCH t.assignedTo WHERE LOWER(t.assignedTo.email) = LOWER(:email) ORDER BY t.weekNumber ASC, t.id ASC")
    List<CleaningTask> findByAssignedTo_EmailIgnoreCaseOrderByWeekNumberAscIdAsc(@Param("email") String email);

    @Query("""
            SELECT t FROM CleaningTask t
            WHERE t.deadline < :today
              AND t.completed = false
              AND t.assignedTo IS NOT NULL
            ORDER BY t.deadline ASC
            """)
    List<CleaningTask> findOverdueTasks(@Param("today") LocalDate today);

    List<CleaningTask> findByAssignedTo(User user);

    List<CleaningTask> findByOrganization_IdOrderByIdAsc(Long organizationId);

    @Modifying
    @Query("UPDATE CleaningTask t SET t.assignedTo = NULL WHERE t.assignedTo = :user")
    void unassignAllForUser(@Param("user") User user);

    @Modifying
    @Query("DELETE FROM CleaningTask t")
    void deleteAllTasks();

    /**
     * Org-gescoped vervanger van deleteAllTasks(): die methode verwijderde
     * ooit ALLE cleaning tasks van elke organisatie tegelijk (zie
     * CleaningScheduleService.reseedNow()). Zodra de service-laag een
     * organizationId beschikbaar heeft (Fase 3), moet elke aanroep hierheen
     * verhuizen en moet deleteAllTasks() zelf verdwijnen.
     */
    @Modifying
    @Query("DELETE FROM CleaningTask t WHERE t.organization.id = :organizationId")
    void deleteAllTasksForOrganization(@Param("organizationId") Long organizationId);
}