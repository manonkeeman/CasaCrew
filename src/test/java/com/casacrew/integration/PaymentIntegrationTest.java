package com.casacrew.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.casacrew.model.Organization;
import com.casacrew.model.Payment;
import com.casacrew.model.Room;
import com.casacrew.model.User;
import com.casacrew.repository.OrganizationRepository;
import com.casacrew.repository.PaymentRepository;
import com.casacrew.repository.RoomRepository;
import com.casacrew.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PaymentIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminPassword;
    private Organization organization;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        organization = organizationRepository.findBySlugIgnoreCase("casacrew")
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

    private String loginAndGetToken() throws Exception {
        var payload = objectMapper.createObjectNode();
        payload.put("email", ADMIN_EMAIL);
        payload.put("password", adminPassword);

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
                .andReturn()
                .getResponse()
                .getContentAsString();

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

    private Payment savePayment(User student, BigDecimal amount, Payment.PaymentStatus status, String description) {
        Payment payment = new Payment(amount, null, status, description, student);
        payment.setOrganization(organization);
        return paymentRepository.save(payment);
    }

    @Test
    void getAllPayments_asAdmin_returns200AndIncludesSeededPayment() throws Exception {
        String password = UUID.randomUUID().toString();
        User student = saveStudentWithRoom("student", "s1@test.com", password, "Kamer 1");
        savePayment(student, new BigDecimal("100.00"), Payment.PaymentStatus.OPEN, "Testbetaling");

        String adminToken = loginAndGetToken();

        mockMvc.perform(get("/api/payments")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentEmail").value("s1@test.com"));
    }

    @Test
    void getAllPayments_asStudent_returns403() throws Exception {
        String password = UUID.randomUUID().toString();
        saveStudentWithRoom("student", "s2@test.com", password, "Kamer 2");
        String studentToken = loginAsStudentAndGetToken("s2@test.com", password, "Kamer 2");

        mockMvc.perform(get("/api/payments")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllPayments_withoutAuth_returns401or403() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/payments")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isIn(401, 403);
    }

    @Test
    void getMyPayments_asStudent_returnsOnlyOwnPayments() throws Exception {
        String password1 = UUID.randomUUID().toString();
        User student1 = saveStudentWithRoom("student1", "own@test.com", password1, "Kamer 3");
        savePayment(student1, new BigDecimal("50.00"), Payment.PaymentStatus.OPEN, "Eigen betaling");

        String password2 = UUID.randomUUID().toString();
        User student2 = saveStudentWithRoom("student2", "other@test.com", password2, "Kamer 4");
        savePayment(student2, new BigDecimal("75.00"), Payment.PaymentStatus.OPEN, "Andermans betaling");

        String studentToken = loginAsStudentAndGetToken("own@test.com", password1, "Kamer 3");

        mockMvc.perform(get("/api/payments/me")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].studentEmail").value("own@test.com"));
    }

    @Test
    void createPayment_asAdmin_validBody_returns201() throws Exception {
        String password = UUID.randomUUID().toString();
        saveStudentWithRoom("student", "new@test.com", password, "Kamer 5");

        String adminToken = loginAndGetToken();

        var payload = objectMapper.createObjectNode();
        payload.put("amount", "42.50");
        payload.put("description", "Wasmachine bijdrage");
        payload.put("status", "OPEN");
        payload.put("studentEmail", "new@test.com");

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.studentEmail").value("new@test.com"))
                .andExpect(jsonPath("$.amount").value(42.50));
    }

    @Test
    void createPayment_asAdmin_missingStudentEmail_returns400() throws Exception {
        String adminToken = loginAndGetToken();

        var payload = objectMapper.createObjectNode();
        payload.put("amount", "42.50");
        payload.put("status", "OPEN");

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPayment_asAdmin_invalidStatus_returns400() throws Exception {
        String password = UUID.randomUUID().toString();
        saveStudentWithRoom("student", "badstatus@test.com", password, "Kamer 6");

        String adminToken = loginAndGetToken();

        var payload = objectMapper.createObjectNode();
        payload.put("amount", "42.50");
        payload.put("status", "NOT_A_STATUS");
        payload.put("studentEmail", "badstatus@test.com");

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPayment_asStudent_returns403() throws Exception {
        String password = UUID.randomUUID().toString();
        saveStudentWithRoom("student", "s3@test.com", password, "Kamer 7");
        String studentToken = loginAsStudentAndGetToken("s3@test.com", password, "Kamer 7");

        var payload = objectMapper.createObjectNode();
        payload.put("amount", "10.00");
        payload.put("status", "OPEN");
        payload.put("studentEmail", "s3@test.com");

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deletePayment_asAdmin_returns204() throws Exception {
        String password = UUID.randomUUID().toString();
        User student = saveStudentWithRoom("student", "del@test.com", password, "Kamer 8");
        Payment payment = savePayment(student, new BigDecimal("30.00"), Payment.PaymentStatus.OPEN, "Te verwijderen");

        String adminToken = loginAndGetToken();

        mockMvc.perform(delete("/api/payments/" + payment.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deletePayment_asStudent_returns403() throws Exception {
        String password = UUID.randomUUID().toString();
        User student = saveStudentWithRoom("student", "del2@test.com", password, "Kamer 9");
        Payment payment = savePayment(student, new BigDecimal("30.00"), Payment.PaymentStatus.OPEN, "Te verwijderen");

        String studentToken = loginAsStudentAndGetToken("del2@test.com", password, "Kamer 9");

        mockMvc.perform(delete("/api/payments/" + payment.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }
}
