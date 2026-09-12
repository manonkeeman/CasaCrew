package com.casacrew.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.casacrew.model.Organization;
import com.casacrew.model.User;
import com.casacrew.repository.OrganizationRepository;
import com.casacrew.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminPassword;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        Organization organization = organizationRepository.findBySlugIgnoreCase("casacrew")
                .orElseGet(() -> organizationRepository.save(new Organization("CasaCrew", "casacrew-" + UUID.randomUUID())));

        adminPassword = UUID.randomUUID().toString();

        User admin = new User(
                ADMIN_USERNAME,
                ADMIN_EMAIL,
                passwordEncoder.encode(adminPassword),
                User.Role.ADMIN
        );
        admin.setOrganization(organization);

        userRepository.save(admin);
    }

    @Test
    void login_missingEmailAndPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_invalidEmailFormat_returns400() throws Exception {
        var payload = objectMapper.createObjectNode();
        payload.put("email", "not-an-email");
        payload.put("password", "x"); // geen echte password, alleen voor validatiepad

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_validCredentials_returnsJwtToken() throws Exception {
        var payload = objectMapper.createObjectNode();
        payload.put("email", ADMIN_EMAIL);
        payload.put("password", adminPassword);

        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body).contains("token");
    }
}