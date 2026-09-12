package com.casacrew.integration;

import com.casacrew.model.Organization;
import com.casacrew.model.User;
import com.casacrew.repository.OrganizationRepository;
import com.casacrew.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

class UserIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminPassword;
    private String studentEmail;
    private String studentPassword;
    private Organization organization;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        organization = organizationRepository.save(new Organization("CasaCrew Test", "casacrew-test-" + UUID.randomUUID()));

        adminPassword = UUID.randomUUID().toString();
        User admin = new User(ADMIN_USERNAME, ADMIN_EMAIL, passwordEncoder.encode(adminPassword), User.Role.ADMIN);
        admin.setOrganization(organization);
        userRepository.save(admin);

        studentEmail = "student-" + UUID.randomUUID() + "@test.com";
        studentPassword = UUID.randomUUID().toString();
        User student = new User("student", studentEmail, passwordEncoder.encode(studentPassword), User.Role.STUDENT);
        student.setOrganization(organization);
        userRepository.save(student);
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        var payload = objectMapper.createObjectNode();
        payload.put("email", email);
        payload.put("password", password);

        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(body).get("token").asText();
    }

    @Test
    void getAllUsers_asAdmin_returns200() throws Exception {
        String token = loginAndGetToken(ADMIN_EMAIL, adminPassword);

        mockMvc.perform(get("/api/users")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void getAllUsers_asStudent_returns403() throws Exception {
        String token = loginAndGetToken(studentEmail, studentPassword);

        mockMvc.perform(get("/api/users")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllUsers_withoutAuth_returns401or403() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isIn(401, 403);
    }

    @Test
    void me_asStudent_returnsOwnProfile() throws Exception {
        String token = loginAndGetToken(studentEmail, studentPassword);

        mockMvc.perform(get("/api/users/me")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(studentEmail))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    void createUser_asAdmin_validBody_returns201() throws Exception {
        String token = loginAndGetToken(ADMIN_EMAIL, adminPassword);

        var payload = objectMapper.createObjectNode();
        payload.put("username", "nieuwecleaner");
        payload.put("email", "cleaner-" + UUID.randomUUID() + "@test.com");
        payload.put("password", "supersecret123");
        payload.put("role", "CLEANER");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("nieuwecleaner"))
                .andExpect(jsonPath("$.role").value("CLEANER"));
    }

    @Test
    void createUser_asAdmin_invalidRole_returns400() throws Exception {
        String token = loginAndGetToken(ADMIN_EMAIL, adminPassword);

        var payload = objectMapper.createObjectNode();
        payload.put("username", "iemand");
        payload.put("email", "iemand-" + UUID.randomUUID() + "@test.com");
        payload.put("password", "supersecret123");
        payload.put("role", "SUPERADMIN");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_asStudent_returns403() throws Exception {
        String token = loginAndGetToken(studentEmail, studentPassword);

        var payload = objectMapper.createObjectNode();
        payload.put("username", "iemand");
        payload.put("email", "iemand-" + UUID.randomUUID() + "@test.com");
        payload.put("password", "supersecret123");
        payload.put("role", "STUDENT");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getById_asAdmin_returns200() throws Exception {
        String token = loginAndGetToken(ADMIN_EMAIL, adminPassword);
        User student = userRepository.findByEmailIgnoreCase(studentEmail).orElseThrow();

        mockMvc.perform(get("/api/users/" + student.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(studentEmail));
    }

    @Test
    void housemates_asStudent_returns200() throws Exception {
        String token = loginAndGetToken(studentEmail, studentPassword);

        mockMvc.perform(get("/api/users/housemates")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void housemates_asAdmin_returns403() throws Exception {
        String token = loginAndGetToken(ADMIN_EMAIL, adminPassword);

        mockMvc.perform(get("/api/users/housemates")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_asAdmin_success_returns204() throws Exception {
        String token = loginAndGetToken(ADMIN_EMAIL, adminPassword);
        User student = userRepository.findByEmailIgnoreCase(studentEmail).orElseThrow();

        mockMvc.perform(delete("/api/users/" + student.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_asStudent_returns403() throws Exception {
        String token = loginAndGetToken(studentEmail, studentPassword);
        User admin = userRepository.findByEmailIgnoreCase(ADMIN_EMAIL).orElseThrow();

        mockMvc.perform(delete("/api/users/" + admin.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
