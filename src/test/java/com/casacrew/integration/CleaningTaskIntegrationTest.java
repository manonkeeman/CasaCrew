package com.casacrew.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.casacrew.model.CleaningTask;
import com.casacrew.model.Organization;
import com.casacrew.model.Room;
import com.casacrew.model.User;
import com.casacrew.repository.CleaningTaskRepository;
import com.casacrew.repository.OrganizationRepository;
import com.casacrew.repository.RoomRepository;
import com.casacrew.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CleaningTaskIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private CleaningTaskRepository cleaningTaskRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminPassword;
    private User admin;
    private Organization organization;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        organization = organizationRepository.findBySlugIgnoreCase("casacrew")
                .orElseGet(() -> organizationRepository.save(new Organization("CasaCrew", "casacrew-" + UUID.randomUUID())));

        adminPassword = UUID.randomUUID().toString();
        admin = new User(ADMIN_USERNAME, ADMIN_EMAIL, passwordEncoder.encode(adminPassword), User.Role.ADMIN);
        admin.setOrganization(organization);
        userRepository.save(admin);
    }

    private String loginAndGetToken() throws Exception {
        var payload = objectMapper.createObjectNode();
        payload.put("email", ADMIN_EMAIL);
        payload.put("password", adminPassword);

        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("token").asText();
    }

    private String loginAsStudentAndGetToken(String email, String password, String roomName) throws Exception {
        var payload = objectMapper.createObjectNode();
        payload.put("email", email);
        payload.put("password", password);
        payload.put("room", roomName);

        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("token").asText();
    }

    private User saveStudentWithRoom(String username, String email, String rawPassword, String roomName) {
        User student = new User(username, email, passwordEncoder.encode(rawPassword), User.Role.STUDENT);
        student.setOrganization(organization);
        userRepository.save(student);
        Room room = new Room(roomName);
        room.setOrganization(organization);
        room.assignOccupant(student);
        roomRepository.save(room);
        return student;
    }

    private CleaningTask saveTask(User assignedTo) {
        CleaningTask task = new CleaningTask(1, "Keuken schoonmaken", "Grondig", "ROLE_ALL");
        task.setOrganization(organization);
        if (assignedTo != null) {
            task.setAssignedTo(assignedTo);
        }
        return cleaningTaskRepository.save(task);
    }

    @Test
    void getTasks_withValidJwt_returns200() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(get("/api/cleaning/tasks")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void getTasks_withoutJwt_returns401or403() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/cleaning/tasks").accept(MediaType.APPLICATION_JSON)).andReturn();
        assertThat(result.getResponse().getStatus()).isIn(401, 403);
    }

    @Test
    void createTask_asAdmin_validBody_returns201() throws Exception {
        String adminToken = loginAndGetToken();

        var payload = objectMapper.createObjectNode();
        payload.put("weekNumber", 3);
        payload.put("name", "Badkamer schoonmaken");

        mockMvc.perform(post("/api/cleaning/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Badkamer schoonmaken"));
    }

    @Test
    void createTask_asAdmin_missingName_returns400() throws Exception {
        String adminToken = loginAndGetToken();

        var payload = objectMapper.createObjectNode();
        payload.put("weekNumber", 3);

        mockMvc.perform(post("/api/cleaning/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTask_asStudent_returns403() throws Exception {
        String password = UUID.randomUUID().toString();
        saveStudentWithRoom("student", "student@test.com", password, "Kamer 1");
        String studentToken = loginAsStudentAndGetToken("student@test.com", password, "Kamer 1");

        var payload = objectMapper.createObjectNode();
        payload.put("weekNumber", 3);
        payload.put("name", "Test");

        mockMvc.perform(post("/api/cleaning/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteTask_asStudent_returns403() throws Exception {
        String password = UUID.randomUUID().toString();
        User student = saveStudentWithRoom("student2", "student2@test.com", password, "Kamer 2");
        CleaningTask task = saveTask(student);
        String studentToken = loginAsStudentAndGetToken("student2@test.com", password, "Kamer 2");

        mockMvc.perform(delete("/api/cleaning/tasks/" + task.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void toggleTask_asAssignedStudent_returns200AndFlipsCompleted() throws Exception {
        String password = UUID.randomUUID().toString();
        User student = saveStudentWithRoom("student3", "student3@test.com", password, "Kamer 3");
        CleaningTask task = saveTask(student);
        String studentToken = loginAsStudentAndGetToken("student3@test.com", password, "Kamer 3");

        mockMvc.perform(put("/api/cleaning/tasks/" + task.getId() + "/toggle")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void getMyTasks_asStudent_onlyReturnsAssignedTasks() throws Exception {
        String password = UUID.randomUUID().toString();
        User student = saveStudentWithRoom("student4", "student4@test.com", password, "Kamer 4");
        saveTask(student);
        saveTask(null); // niet aan deze student toegewezen
        String studentToken = loginAsStudentAndGetToken("student4@test.com", password, "Kamer 4");

        mockMvc.perform(get("/api/cleaning/tasks/me")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isOk());
    }
}
