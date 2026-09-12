package com.casacrew.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.casacrew.model.Organization;
import com.casacrew.model.User;
import com.casacrew.repository.OrganizationRepository;
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

class ExpenseIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminPassword;
    private String studentEmail;
    private String studentPassword;

    @BeforeEach
    void setup() throws Exception {
        userRepository.deleteAll();

        Organization organization = organizationRepository.save(new Organization("CasaCrew Test", "casacrew-test-" + UUID.randomUUID()));

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
    void getAll_asAdmin_returns200() throws Exception {
        String token = loginAndGetToken(ADMIN_EMAIL, adminPassword);

        mockMvc.perform(get("/api/admin/expenses")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void getAll_withoutAuth_returns401or403() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/expenses")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isIn(401, 403);
    }

    @Test
    void getAll_asStudent_returns403() throws Exception {
        String token = loginAndGetToken(studentEmail, studentPassword);

        mockMvc.perform(get("/api/admin/expenses")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_asAdmin_validBody_returns200() throws Exception {
        String token = loginAndGetToken(ADMIN_EMAIL, adminPassword);

        var payload = objectMapper.createObjectNode();
        payload.put("category", "ONDERHOUD");
        payload.put("description", "Nieuwe kraan");
        payload.put("amount", "49.95");
        payload.put("expenseDate", "2025-01-15");

        mockMvc.perform(post("/api/admin/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Nieuwe kraan"))
                .andExpect(jsonPath("$.category").value("ONDERHOUD"));
    }

    @Test
    void create_asAdmin_invalidCategory_returns400() throws Exception {
        String token = loginAndGetToken(ADMIN_EMAIL, adminPassword);

        var payload = objectMapper.createObjectNode();
        payload.put("category", "NIET_BESTAAND");
        payload.put("description", "Iets");
        payload.put("amount", "10.00");
        payload.put("expenseDate", "2025-01-15");

        mockMvc.perform(post("/api/admin/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_asAdmin_missingFields_returns400() throws Exception {
        String token = loginAndGetToken(ADMIN_EMAIL, adminPassword);

        var payload = objectMapper.createObjectNode();
        payload.put("category", "ONDERHOUD");

        mockMvc.perform(post("/api/admin/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_asStudent_returns403() throws Exception {
        String token = loginAndGetToken(studentEmail, studentPassword);

        var payload = objectMapper.createObjectNode();
        payload.put("category", "ONDERHOUD");
        payload.put("description", "Iets");
        payload.put("amount", "10.00");
        payload.put("expenseDate", "2025-01-15");

        mockMvc.perform(post("/api/admin/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_withoutAuth_returns401or403() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isIn(401, 403);
    }

    @Test
    void deleteExpense_asAdmin_success_returns204() throws Exception {
        String token = loginAndGetToken(ADMIN_EMAIL, adminPassword);

        var payload = objectMapper.createObjectNode();
        payload.put("category", "OVERIG");
        payload.put("description", "Te verwijderen");
        payload.put("amount", "5.00");
        payload.put("expenseDate", "2025-01-15");

        String body = mockMvc.perform(post("/api/admin/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long id = objectMapper.readTree(body).get("id").asLong();

        mockMvc.perform(delete("/api/admin/expenses/" + id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void exportCsv_asAdmin_returns200WithCsvContentType() throws Exception {
        String token = loginAndGetToken(ADMIN_EMAIL, adminPassword);

        mockMvc.perform(get("/api/admin/expenses/export.csv")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.valueOf("text/csv")));
    }
}
