package com.casacrew.controller;

import com.casacrew.dto.CleaningTaskRequestDTO;
import com.casacrew.dto.CleaningTaskResponseDTO;
import com.casacrew.service.CleaningTaskService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@Validated
@RestController
@RequestMapping("/api/cleaning")
public class CleaningTaskController {

    private final CleaningTaskService cleaningService;

    public CleaningTaskController(CleaningTaskService cleaningService) {
        this.cleaningService = cleaningService;
    }

    @GetMapping("/schedule/info")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT','CLEANER')")
    public ResponseEntity<Map<String, Integer>> getScheduleInfo() {
        Map<String, Integer> info = new LinkedHashMap<>();
        info.put("isoWeek",        cleaningService.getCurrentIsoWeek());
        info.put("rotationWeek",   cleaningService.getCurrentRotationWeek());
        info.put("rotationLength", cleaningService.getRotationLength());
        info.put("year",           cleaningService.getCurrentIsoYear());
        return ResponseEntity.ok(info);
    }

    @GetMapping("/tasks/test-cleaner")
    @PreAuthorize("hasRole('CLEANER')")
    public ResponseEntity<String> cleanerAccessCheck() {
        return ResponseEntity.ok("CLEANER OK");
    }

    @GetMapping("/tasks/me")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT','CLEANER')")
    public ResponseEntity<List<CleaningTaskResponseDTO>> getMyTasks(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(cleaningService.getTasksForCaller(email));
    }

    @GetMapping("/tasks")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT','CLEANER')")
    public ResponseEntity<List<CleaningTaskResponseDTO>> getTasks(
            @RequestParam(required = false) @Positive Integer weekNumber,
            Authentication authentication
    ) {
        String role = resolveRole(authentication);

        List<CleaningTaskResponseDTO> result = (weekNumber == null)
                ? cleaningService.getAllTasksForRole(role)
                : cleaningService.getTasksByWeekForRole(role, weekNumber);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/tasks")
    @PreAuthorize("hasAnyRole('ADMIN','CLEANER')")
    public ResponseEntity<CleaningTaskResponseDTO> createTask(
            @Valid @RequestBody CleaningTaskRequestDTO dto
    ) {
        CleaningTaskResponseDTO created = cleaningService.addTask(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/tasks/{taskId}/toggle")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT','CLEANER')")
    public ResponseEntity<CleaningTaskResponseDTO> toggleTask(
            @PathVariable @Positive Long taskId
    ) {
        return ResponseEntity.ok(cleaningService.toggleTask(taskId));
    }

    @PutMapping("/tasks/{taskId}/comment")
    @PreAuthorize("hasAnyRole('ADMIN','CLEANER')")
    public ResponseEntity<CleaningTaskResponseDTO> addComment(
            @PathVariable @Positive Long taskId,
            @RequestParam @NotBlank String comment
    ) {
        return ResponseEntity.ok(cleaningService.addComment(taskId, comment));
    }

    @PutMapping("/tasks/{taskId}/incident")
    @PreAuthorize("hasAnyRole('ADMIN','CLEANER')")
    public ResponseEntity<CleaningTaskResponseDTO> addIncident(
            @PathVariable @Positive Long taskId,
            @RequestParam @NotBlank String incident
    ) {
        return ResponseEntity.ok(cleaningService.addIncident(taskId, incident));
    }

    @DeleteMapping("/tasks/{taskId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTask(@PathVariable @Positive Long taskId) {
        cleaningService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    private String resolveRole(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return "STUDENT";
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (isAdmin) return "ADMIN";

        boolean isCleaner = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_CLEANER".equals(a.getAuthority()));
        if (isCleaner) return "CLEANER";

        return "STUDENT";
    }
}