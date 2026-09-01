package com.casacrew.repository;

import com.casacrew.model.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    List<CalendarEvent> findByOrganization_IdOrderByEventDateAscEventTimeAsc(Long organizationId);
}
