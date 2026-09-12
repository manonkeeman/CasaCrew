package com.casacrew.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrganizationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        // no admin fixture needed: registration is self-service and permitAll
    }

    @Test
    void register_validBody_returns201WithSessionToken() throws Exception {
        String email = "owner-" + UUID.randomUUID() + "@test.com";

        var payload = objectMapper.createObjectNode();
        payload.put("organizationName", "Casa Test " + UUID.randomUUID());
        payload.put("adminUsername", "owner");
        payload.put("adminEmail", email);
        payload.put("adminPassword", "supersecret123");

        String body = mockMvc.perform(post("/api/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(body);
        assertThat(json.get("token").asText()).as("Registratie moet meteen een sessietoken teruggeven").isNotBlank();
    }

    @Test
    void register_missingRequiredFields_returns400() throws Exception {
        var payload = objectMapper.createObjectNode();
        payload.put("organizationName", "Casa Test");
        // adminUsername/adminEmail/adminPassword ontbreken

        mockMvc.perform(post("/api/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_passwordTooShort_returns400() throws Exception {
        var payload = objectMapper.createObjectNode();
        payload.put("organizationName", "Casa Test");
        payload.put("adminUsername", "owner");
        payload.put("adminEmail", "owner-" + UUID.randomUUID() + "@test.com");
        payload.put("adminPassword", "short");

        mockMvc.perform(post("/api/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_duplicateEmail_returns409or400() throws Exception {
        String email = "dup-" + UUID.randomUUID() + "@test.com";

        var payload = objectMapper.createObjectNode();
        payload.put("organizationName", "Casa Test A");
        payload.put("adminUsername", "owner");
        payload.put("adminEmail", email);
        payload.put("adminPassword", "supersecret123");

        mockMvc.perform(post("/api/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated());

        var duplicatePayload = objectMapper.createObjectNode();
        duplicatePayload.put("organizationName", "Casa Test B");
        duplicatePayload.put("adminUsername", "owner2");
        duplicatePayload.put("adminEmail", email);
        duplicatePayload.put("adminPassword", "supersecret123");

        var result = mockMvc.perform(post("/api/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicatePayload)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isIn(400, 409);
    }
}
