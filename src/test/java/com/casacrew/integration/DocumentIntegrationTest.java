package com.casacrew.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.casacrew.model.Document;
import com.casacrew.model.Organization;
import com.casacrew.model.Room;
import com.casacrew.model.User;
import com.casacrew.repository.DocumentRepository;
import com.casacrew.repository.OrganizationRepository;
import com.casacrew.repository.RoomRepository;
import com.casacrew.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DocumentIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminPassword;
    private Organization organization;
    private User admin;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        documentRepository.deleteAll();

        organization = organizationRepository.findBySlugIgnoreCase("casacrew")
                .orElseGet(() -> organizationRepository.save(new Organization("CasaCrew", "casacrew-" + UUID.randomUUID())));

        adminPassword = UUID.randomUUID().toString();

        admin = new User(
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

    private Document saveDocument(String title, String roleAccess) {
        Document document = new Document(
                title,
                "Testdocument",
                "/tmp/does-not-need-to-exist-for-listing-" + UUID.randomUUID(),
                roleAccess,
                admin
        );
        document.setOrganization(organization);
        return documentRepository.save(document);
    }

    @Test
    void uploadDocument_asAdmin_validFile_returns201() throws Exception {
        String adminToken = loginAndGetToken();

        MockMultipartFile file = new MockMultipartFile(
                "file", "huisregels.txt", "text/plain", "Wees aardig voor elkaar.".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/documents")
                        .file(file)
                        .part(new MockPart("roleAccess", "STUDENT".getBytes(StandardCharsets.UTF_8)))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documentId").exists())
                .andExpect(jsonPath("$.downloadUrl").value(org.hamcrest.Matchers.containsString("/download")));
    }

    @Test
    void uploadDocument_asStudent_returns403() throws Exception {
        String password = UUID.randomUUID().toString();
        saveStudentWithRoom("student", "upl@test.com", password, "Kamer 1");
        String studentToken = loginAsStudentAndGetToken("upl@test.com", password, "Kamer 1");

        MockMultipartFile file = new MockMultipartFile(
                "file", "huisregels.txt", "text/plain", "Inhoud".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/documents")
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getDocuments_asStudent_onlyReturnsAccessibleDocuments() throws Exception {
        saveDocument("Voor studenten", "STUDENT");
        saveDocument("Voor schoonmakers", "CLEANER");
        saveDocument("Voor iedereen", Document.ROLE_ALL);

        String password = UUID.randomUUID().toString();
        saveStudentWithRoom("student", "list@test.com", password, "Kamer 2");
        String studentToken = loginAsStudentAndGetToken("list@test.com", password, "Kamer 2");

        mockMvc.perform(get("/api/documents")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].title", org.hamcrest.Matchers.hasItems("Voor studenten", "Voor iedereen")));
    }

    @Test
    void downloadDocument_asAllowedRole_returns200() throws Exception {
        String adminToken = loginAndGetToken();

        MockMultipartFile file = new MockMultipartFile(
                "file", "voor-studenten.txt", "text/plain", "Studenteninhoud".getBytes(StandardCharsets.UTF_8));

        String uploadBody = mockMvc.perform(multipart("/api/documents")
                        .file(file)
                        .part(new MockPart("roleAccess", "STUDENT".getBytes(StandardCharsets.UTF_8)))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(uploadBody);
        long documentId = json.get("documentId").asLong();

        String password = UUID.randomUUID().toString();
        saveStudentWithRoom("student", "dl@test.com", password, "Kamer 3");
        String studentToken = loginAsStudentAndGetToken("dl@test.com", password, "Kamer 3");

        mockMvc.perform(get("/api/documents/" + documentId + "/download")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isOk());
    }

    @Test
    void downloadDocument_asDisallowedRole_returns404() throws Exception {
        String adminToken = loginAndGetToken();

        MockMultipartFile file = new MockMultipartFile(
                "file", "voor-schoonmakers.txt", "text/plain", "Schoonmaakinhoud".getBytes(StandardCharsets.UTF_8));

        String uploadBody = mockMvc.perform(multipart("/api/documents")
                        .file(file)
                        .part(new MockPart("roleAccess", "CLEANER".getBytes(StandardCharsets.UTF_8)))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(uploadBody);
        long documentId = json.get("documentId").asLong();

        String password = UUID.randomUUID().toString();
        saveStudentWithRoom("student", "nodl@test.com", password, "Kamer 4");
        String studentToken = loginAsStudentAndGetToken("nodl@test.com", password, "Kamer 4");

        mockMvc.perform(get("/api/documents/" + documentId + "/download")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteDocument_asAdmin_returns204() throws Exception {
        Document document = saveDocument("Te verwijderen", Document.ROLE_ALL);
        String adminToken = loginAndGetToken();

        mockMvc.perform(delete("/api/documents/" + document.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteDocument_asStudent_returns403() throws Exception {
        Document document = saveDocument("Blijft staan", Document.ROLE_ALL);

        String password = UUID.randomUUID().toString();
        saveStudentWithRoom("student", "nodel@test.com", password, "Kamer 5");
        String studentToken = loginAsStudentAndGetToken("nodel@test.com", password, "Kamer 5");

        mockMvc.perform(delete("/api/documents/" + document.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }
}
