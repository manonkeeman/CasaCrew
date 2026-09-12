package com.casacrew.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.casacrew.model.MaintenanceRequest;
import com.casacrew.model.Organization;
import com.casacrew.model.Room;
import com.casacrew.model.User;
import com.casacrew.repository.MaintenanceRequestRepository;
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MaintenanceRequestIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private MaintenanceRequestRepository maintenanceRequestRepository;

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

    private MaintenanceRequest saveRequest(User reporter, String title) {
        MaintenanceRequest request = new MaintenanceRequest();
        request.setTitle(title);
        request.setDescription("Details over " + title);
        request.setUrgency("MEDIUM");
        request.setStatus("OPEN");
        request.setOrganization(organization);
        request.setReportedBy(reporter);
        return maintenanceRequestRepository.save(request);
    }

    @Test
    void getAll_asAdmin_returns200() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(get("/api/maintenance-requests")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void getAll_withoutJwt_returns401or403() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/maintenance-requests").accept(MediaType.APPLICATION_JSON)).andReturn();
        assertThat(result.getResponse().getStatus()).isIn(401, 403);
    }

    @Test
    void getAll_asStudent_returns403() throws Exception {
        String password = UUID.randomUUID().toString();
        saveStudentWithRoom("student", "student@test.com", password, "Kamer 1");
        String studentToken = loginAsStudentAndGetToken("student@test.com", password, "Kamer 1");

        mockMvc.perform(get("/api/maintenance-requests")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_asStudent_validBody_returns200() throws Exception {
        String password = UUID.randomUUID().toString();
        saveStudentWithRoom("student2", "student2@test.com", password, "Kamer 2");
        String studentToken = loginAsStudentAndGetToken("student2@test.com", password, "Kamer 2");

        var payload = objectMapper.createObjectNode();
        payload.put("title", "Lekkende kraan");
        payload.put("description", "De kraan in de keuken lekt.");
        payload.put("location", "Keuken");
        payload.put("urgency", "HIGH");

        mockMvc.perform(post("/api/maintenance-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Lekkende kraan"));
    }

    @Test
    void create_asStudent_missingRequiredFields_returns400() throws Exception {
        String password = UUID.randomUUID().toString();
        saveStudentWithRoom("student3", "student3@test.com", password, "Kamer 3");
        String studentToken = loginAsStudentAndGetToken("student3@test.com", password, "Kamer 3");

        var payload = objectMapper.createObjectNode();
        payload.put("title", "");

        mockMvc.perform(post("/api/maintenance-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_asAdmin_returns403() throws Exception {
        String adminToken = loginAndGetToken();

        var payload = objectMapper.createObjectNode();
        payload.put("title", "Test");
        payload.put("description", "Test omschrijving");

        mockMvc.perform(post("/api/maintenance-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMine_asStudent_onlyReturnsOwnRequests() throws Exception {
        User student1 = saveStudentWithRoom("owner", "owner@test.com", "pass1234pass", "Kamer 4");
        saveRequest(student1, "Verzoek van eigenaar");

        User student2 = saveStudentWithRoom("other", "other@test.com", "pass1234pass", "Kamer 5");
        saveRequest(student2, "Verzoek van ander");

        String ownerToken = loginAsStudentAndGetToken("owner@test.com", "pass1234pass", "Kamer 4");

        mockMvc.perform(get("/api/maintenance-requests/me")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", everyItem(not(equalTo("Verzoek van ander")))));
    }

    @Test
    void updateStatus_asStudent_returns403() throws Exception {
        User student = saveStudentWithRoom("student5", "student5@test.com", "pass1234pass", "Kamer 6");
        MaintenanceRequest request = saveRequest(student, "Test verzoek");
        String studentToken = loginAsStudentAndGetToken("student5@test.com", "pass1234pass", "Kamer 6");

        var payload = objectMapper.createObjectNode();
        payload.put("status", "RESOLVED");

        mockMvc.perform(put("/api/maintenance-requests/" + request.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateStatus_asAdmin_validBody_returns200() throws Exception {
        User student = saveStudentWithRoom("student6", "student6@test.com", "pass1234pass", "Kamer 7");
        MaintenanceRequest request = saveRequest(student, "Test verzoek 2");
        String adminToken = loginAndGetToken();

        var payload = objectMapper.createObjectNode();
        payload.put("status", "IN_PROGRESS");
        payload.put("adminNote", "Wordt opgepakt.");

        mockMvc.perform(put("/api/maintenance-requests/" + request.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }
}
