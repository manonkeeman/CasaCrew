package com.casacrew.service;

import com.casacrew.dto.DocumentResponseDTO;
import com.casacrew.dto.UploadResponseDTO;
import com.casacrew.model.Document;
import com.casacrew.model.Organization;
import com.casacrew.model.User;
import com.casacrew.repository.DocumentRepository;
import com.casacrew.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    private static final Long ORG_ID = 1L;

    @Mock DocumentRepository documentRepository;
    @Mock UserRepository userRepository;
    @Mock MailService mailService;
    @Mock UserService userService;
    @InjectMocks DocumentService documentService;

    @TempDir Path tempDir;

    @BeforeEach
    void setUploadDir() {
        ReflectionTestUtils.setField(documentService, "uploadDirPath", tempDir.toString());
    }

    private Organization makeOrganization() {
        Organization organization = new Organization("CasaCrew", "casacrew");
        ReflectionTestUtils.setField(organization, "id", ORG_ID);
        return organization;
    }

    private User makeUser(long id, String username, String email, User.Role role) {
        User user = new User(username, email, "hash", role);
        user.setOrganization(makeOrganization());
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Document makeDocument(long id, String roleAccess, User uploadedBy, String storagePath) {
        Document document = new Document("bestand.pdf", "desc", storagePath, roleAccess, uploadedBy);
        document.setOrganization(uploadedBy.getOrganization());
        ReflectionTestUtils.setField(document, "id", id);
        return document;
    }


    @Test
    void upload_success_writesFileAndSavesDocument() {
        User admin = makeUser(1L, "admin", "a@test.com", User.Role.ADMIN);
        when(userRepository.findByEmailIgnoreCase("a@test.com")).thenReturn(Optional.of(admin));
        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> {
            Document d = inv.getArgument(0);
            ReflectionTestUtils.setField(d, "id", 1L);
            return d;
        });
        when(userRepository.findByOrganization_IdAndRole(ORG_ID, User.Role.STUDENT)).thenReturn(List.of());

        MockMultipartFile file = new MockMultipartFile("file", "huisregels.pdf", "application/pdf", "content".getBytes(StandardCharsets.UTF_8));

        UploadResponseDTO result = documentService.upload("a@test.com", file, "ROLE_ALL");

        assertThat(result.title()).isEqualTo("huisregels.pdf");
        assertThat(result.downloadUrl()).isEqualTo("/api/documents/1/download");
        verify(documentRepository).save(any(Document.class));
    }

    @Test
    void upload_notifiesStudentsWhenRoleAccessAllowsThem() {
        User admin = makeUser(1L, "admin", "a@test.com", User.Role.ADMIN);
        User student = makeUser(2L, "student", "s@test.com", User.Role.STUDENT);
        when(userRepository.findByEmailIgnoreCase("a@test.com")).thenReturn(Optional.of(admin));
        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findByOrganization_IdAndRole(ORG_ID, User.Role.STUDENT)).thenReturn(List.of(student));

        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "content".getBytes(StandardCharsets.UTF_8));

        documentService.upload("a@test.com", file, "STUDENT");

        verify(mailService).sendMailWithRole(eq("ADMIN"), eq("s@test.com"), anyString(), anyString());
    }

    @Test
    void upload_doesNotNotifyStudentsWhenRoleAccessIsAdminOnly() {
        User admin = makeUser(1L, "admin", "a@test.com", User.Role.ADMIN);
        when(userRepository.findByEmailIgnoreCase("a@test.com")).thenReturn(Optional.of(admin));
        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));

        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "content".getBytes(StandardCharsets.UTF_8));

        documentService.upload("a@test.com", file, "ADMIN");

        verify(mailService, never()).sendMailWithRole(any(), any(), any(), any());
        verify(userRepository, never()).findByOrganization_IdAndRole(any(), any());
    }

    @Test
    void upload_blankUploaderPrincipal_throwsIllegalArgumentException() {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "x".getBytes());

        assertThrows(IllegalArgumentException.class, () -> documentService.upload("  ", file, "ROLE_ALL"));
        verify(documentRepository, never()).save(any());
    }

    @Test
    void upload_emptyFile_throwsIllegalArgumentException() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[0]);

        assertThrows(IllegalArgumentException.class, () -> documentService.upload("a@test.com", emptyFile, "ROLE_ALL"));
        verify(documentRepository, never()).save(any());
    }

    @Test
    void upload_uploaderNotFound_throwsEntityNotFoundException() {
        when(userRepository.findByEmailIgnoreCase("missing@test.com")).thenReturn(Optional.empty());
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "x".getBytes());

        assertThrows(EntityNotFoundException.class, () -> documentService.upload("missing@test.com", file, "ROLE_ALL"));
    }

    @Test
    void upload_invalidRoleAccess_throwsIllegalArgumentException() {
        User admin = makeUser(1L, "admin", "a@test.com", User.Role.ADMIN);
        when(userRepository.findByEmailIgnoreCase("a@test.com")).thenReturn(Optional.of(admin));
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "x".getBytes());

        assertThrows(IllegalArgumentException.class, () -> documentService.upload("a@test.com", file, "BOGUS_ROLE"));
        verify(documentRepository, never()).save(any());
    }

    @Test
    void uploadByUserId_invalidId_throwsIllegalArgumentException() {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "x".getBytes());

        assertThrows(IllegalArgumentException.class, () -> documentService.upload((Long) 0L, file, "ROLE_ALL"));
        verify(userRepository, never()).findById(any());
    }

    @Test
    void uploadByUserId_userNotFound_throwsEntityNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "x".getBytes());

        assertThrows(EntityNotFoundException.class, () -> documentService.upload((Long) 99L, file, "ROLE_ALL"));
    }


    @Test
    void listAccessibleDocuments_admin_returnsAllOrgDocuments() {
        when(userService.currentOrganizationId()).thenReturn(ORG_ID);
        User admin = makeUser(1L, "admin", "a@test.com", User.Role.ADMIN);
        Document doc = makeDocument(1L, "ROLE_ALL", admin, tempDir.resolve("f.pdf").toString());
        when(documentRepository.findByOrganization_IdOrderByIdDesc(ORG_ID)).thenReturn(List.of(doc));

        List<DocumentResponseDTO> result = documentService.listAccessibleDocuments("ADMIN");

        assertThat(result).hasSize(1);
        verify(documentRepository, never()).findAccessibleForRoleInOrganization(any(), any());
    }

    @Test
    void listAccessibleDocuments_student_usesRoleScopedQuery() {
        when(userService.currentOrganizationId()).thenReturn(ORG_ID);
        User admin = makeUser(1L, "admin", "a@test.com", User.Role.ADMIN);
        Document doc = makeDocument(1L, "ROLE_ALL", admin, tempDir.resolve("f.pdf").toString());
        when(documentRepository.findAccessibleForRoleInOrganization(ORG_ID, "STUDENT")).thenReturn(List.of(doc));

        List<DocumentResponseDTO> result = documentService.listAccessibleDocuments("STUDENT");

        assertThat(result).hasSize(1);
    }


    @Test
    void download_found_returnsResourceWithTitle() throws IOException {
        when(userService.currentOrganizationId()).thenReturn(ORG_ID);
        User admin = makeUser(1L, "admin", "a@test.com", User.Role.ADMIN);
        Path filePath = tempDir.resolve("bestaand.pdf");
        Files.writeString(filePath, "inhoud");
        Document doc = makeDocument(1L, "ROLE_ALL", admin, filePath.toString());
        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));

        DocumentService.DownloadResult result = documentService.download(1L);

        assertThat(result.title()).isEqualTo("bestand.pdf");
        assertThat(result.resource().exists()).isTrue();
    }

    @Test
    void download_fileMissingOnDisk_throwsEntityNotFoundException() {
        when(userService.currentOrganizationId()).thenReturn(ORG_ID);
        User admin = makeUser(1L, "admin", "a@test.com", User.Role.ADMIN);
        Document doc = makeDocument(1L, "ROLE_ALL", admin, tempDir.resolve("ontbreekt.pdf").toString());
        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));

        assertThrows(EntityNotFoundException.class, () -> documentService.download(1L));
    }

    @Test
    void download_differentOrganization_throwsEntityNotFoundException() {
        when(userService.currentOrganizationId()).thenReturn(2L);
        User admin = makeUser(1L, "admin", "a@test.com", User.Role.ADMIN);
        Document doc = makeDocument(1L, "ROLE_ALL", admin, tempDir.resolve("f.pdf").toString());
        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));

        assertThrows(EntityNotFoundException.class, () -> documentService.download(1L));
    }


    @Test
    void delete_success_deletesFileAndRecord() throws IOException {
        when(userService.currentOrganizationId()).thenReturn(ORG_ID);
        User admin = makeUser(1L, "admin", "a@test.com", User.Role.ADMIN);
        Path filePath = tempDir.resolve("teverwijderen.pdf");
        Files.writeString(filePath, "inhoud");
        Document doc = makeDocument(1L, "ROLE_ALL", admin, filePath.toString());
        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));

        assertDoesNotThrow(() -> documentService.delete(1L));

        assertThat(Files.exists(filePath)).isFalse();
        verify(documentRepository).delete(doc);
    }

    @Test
    void delete_nullId_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> documentService.delete(null));
        verify(documentRepository, never()).delete(any());
    }

    @Test
    void delete_notFound_throwsEntityNotFoundException() {
        when(userService.currentOrganizationId()).thenReturn(ORG_ID);
        when(documentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> documentService.delete(99L));
    }
}
