package com.casacrew.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.casacrew.model.Complaint;
import com.casacrew.model.Organization;
import com.casacrew.model.Room;
import com.casacrew.model.User;
import com.casacrew.repository.ComplaintRepository;
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

class ComplaintIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

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

    private Complaint saveComplaint(User author, String subject) {
        Complaint complaint = new Complaint();
        complaint.setDirection("STUDENT_TO_ADMIN");
        complaint.setSubject(subject);
        complaint.setDescription("Details over " + subject);
        complaint.setStatus("OPEN");
        complaint.setOrganization(organization);
        complaint.setAuthor(author);
        return complaintRepository.save(complaint);
    }

    @Test
    void getAll_asAdmin_returns200() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(get("/api/complaints")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void getAll_withoutJwt_returns401or403() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/complaints").accept(MediaType.APPLICATION_JSON)).andReturn();
        assertThat(result.getResponse().getStatus()).isIn(401, 403);
    }

    @Test
    void getAll_asStudent_returns403() throws Exception {
        String password = UUID.randomUUID().toString();
        saveStudentWithRoom("student", "student@test.com", password, "Kamer 1");
        String studentToken = loginAsStudentAndGetToken("student@test.com", password, "Kamer 1");

        mockMvc.perform(get("/api/complaints")
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
        payload.put("subject", "Kapotte lamp");
        payload.put("description", "De lamp in de gang doet het niet meer.");

        mockMvc.perform(post("/api/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("Kapotte lamp"));
    }

    @Test
    void create_asStudent_missingRequiredFields_returns400() throws Exception {
        String password = UUID.randomUUID().toString();
        saveStudentWithRoom("student3", "student3@test.com", password, "Kamer 3");
        String studentToken = loginAsStudentAndGetToken("student3@test.com", password, "Kamer 3");

        var payload = objectMapper.createObjectNode();
        payload.put("subject", "");

        mockMvc.perform(post("/api/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMine_asStudent_onlyReturnsOwnComplaints() throws Exception {
        User student1 = saveStudentWithRoom("owner", "owner@test.com", "pass1234pass", "Kamer 4");
        saveComplaint(student1, "Klacht van eigenaar");

        User student2 = saveStudentWithRoom("other", "other@test.com", "pass1234pass", "Kamer 5");
        saveComplaint(student2, "Klacht van ander");

        String ownerToken = loginAsStudentAndGetToken("owner@test.com", "pass1234pass", "Kamer 4");

        mockMvc.perform(get("/api/complaints/me")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].subject", org.hamcrest.Matchers.everyItem(
                        org.hamcrest.Matchers.not(org.hamcrest.Matchers.equalTo("Klacht van ander")))));
    }

    @Test
    void updateStatus_asStudent_returns403() throws Exception {
        User student = saveStudentWithRoom("student5", "student5@test.com", "pass1234pass", "Kamer 6");
        Complaint complaint = saveComplaint(student, "Test klacht");
        String studentToken = loginAsStudentAndGetToken("student5@test.com", "pass1234pass", "Kamer 6");

        var payload = objectMapper.createObjectNode();
        payload.put("status", "RESOLVED");

        mockMvc.perform(put("/api/complaints/" + complaint.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateStatus_asAdmin_validBody_returns200() throws Exception {
        User student = saveStudentWithRoom("student6", "student6@test.com", "pass1234pass", "Kamer 7");
        Complaint complaint = saveComplaint(student, "Test klacht 2");
        String adminToken = loginAndGetToken();

        var payload = objectMapper.createObjectNode();
        payload.put("status", "RESOLVED");
        payload.put("response", "Opgelost.");

        mockMvc.perform(put("/api/complaints/" + complaint.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));
    }
}
