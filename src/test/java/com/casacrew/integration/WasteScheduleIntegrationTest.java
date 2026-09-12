package com.casacrew.integration;

import com.casacrew.model.Organization;
import com.casacrew.model.User;
import com.casacrew.model.WasteScheduleEntry;
import com.casacrew.repository.OrganizationRepository;
import com.casacrew.repository.UserRepository;
import com.casacrew.repository.WasteScheduleEntryRepository;
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

class WasteScheduleIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private WasteScheduleEntryRepository wasteScheduleEntryRepository;

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

    private WasteScheduleEntry saveEntry(String wasteType, String scheduleInfo, int orderIndex) {
        WasteScheduleEntry entry = new WasteScheduleEntry(wasteType, scheduleInfo, orderIndex);
        entry.setOrganization(organization);
        return wasteScheduleEntryRepository.save(entry);
    }

    @Test
    void getAll_asStudent_returns200() throws Exception {
        saveEntry("Restafval", "Elke maandag", 0);
        String token = loginAndGetToken(studentEmail, studentPassword);

        mockMvc.perform(get("/api/waste-schedule")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].wasteType").value("Restafval"));
    }

    @Test
    void getAll_withoutAuth_returns401or403() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/waste-schedule")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isIn(401, 403);
    }

    @Test
    void updateScheduleInfo_asAdmin_success_returns200() throws Exception {
        WasteScheduleEntry entry = saveEntry("PMD", "Oud schema", 1);
        String token = loginAndGetToken(ADMIN_EMAIL, adminPassword);

        var payload = objectMapper.createObjectNode();
        payload.put("scheduleInfo", "Elke woensdag");

        mockMvc.perform(put("/api/waste-schedule/" + entry.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduleInfo").value("Elke woensdag"));
    }

    @Test
    void updateScheduleInfo_asStudent_returns403() throws Exception {
        WasteScheduleEntry entry = saveEntry("Papier", "Oud schema", 2);
        String token = loginAndGetToken(studentEmail, studentPassword);

        var payload = objectMapper.createObjectNode();
        payload.put("scheduleInfo", "Iets anders");

        mockMvc.perform(put("/api/waste-schedule/" + entry.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateScheduleInfo_unknownId_returns404() throws Exception {
        String token = loginAndGetToken(ADMIN_EMAIL, adminPassword);

        var payload = objectMapper.createObjectNode();
        payload.put("scheduleInfo", "Iets");

        mockMvc.perform(put("/api/waste-schedule/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNotFound());
    }

    @Test
    void depositPolicy_getThenUpdate_asAdmin_roundTrips() throws Exception {
        String token = loginAndGetToken(ADMIN_EMAIL, adminPassword);

        var payload = objectMapper.createObjectNode();
        payload.put("policy", "Binnen 30 dagen na vertrek");

        mockMvc.perform(put("/api/waste-schedule/deposit-policy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.policy").value("Binnen 30 dagen na vertrek"));

        mockMvc.perform(get("/api/waste-schedule/deposit-policy")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.policy").value("Binnen 30 dagen na vertrek"));
    }

    @Test
    void depositPolicy_update_asStudent_returns403() throws Exception {
        String token = loginAndGetToken(studentEmail, studentPassword);

        var payload = objectMapper.createObjectNode();
        payload.put("policy", "Iets");

        mockMvc.perform(put("/api/waste-schedule/deposit-policy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isForbidden());
    }
}
