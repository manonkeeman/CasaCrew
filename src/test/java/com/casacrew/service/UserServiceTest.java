package com.casacrew.service;

import com.casacrew.dto.HousemateDTO;
import com.casacrew.dto.UserRequestDTO;
import com.casacrew.dto.UserResponseDTO;
import com.casacrew.model.Invoice;
import com.casacrew.model.Organization;
import com.casacrew.model.Room;
import com.casacrew.model.User;
import com.casacrew.repository.CleaningTaskRepository;
import com.casacrew.repository.DocumentRepository;
import com.casacrew.repository.InvoiceRepository;
import com.casacrew.repository.OrganizationRepository;
import com.casacrew.repository.PasswordResetTokenRepository;
import com.casacrew.repository.PaymentRepository;
import com.casacrew.repository.RoomRepository;
import com.casacrew.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final Long ORG_ID = 1L;

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock RoomRepository roomRepository;
    @Mock CleaningTaskRepository cleaningTaskRepository;
    @Mock InvoiceRepository invoiceRepository;
    @Mock DocumentRepository documentRepository;
    @Mock PaymentRepository paymentRepository;
    @Mock PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock AuthSessionService authSessionService;
    @Mock OrganizationRepository organizationRepository;
    @Mock CleaningScheduleService cleaningScheduleService;

    UserService userService;

    @TempDir
    Path tempUploadDir;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                userRepository, passwordEncoder, roomRepository, cleaningTaskRepository,
                invoiceRepository, documentRepository, paymentRepository, passwordResetTokenRepository,
                authSessionService, organizationRepository, cleaningScheduleService,
                tempUploadDir.toString());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
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

    private void authenticateAs(String email, boolean admin) {
        var authorities = admin
                ? List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                : List.of(new SimpleGrantedAuthority("ROLE_STUDENT"));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null, authorities));
    }


    @Test
    void loadUserByUsername_found_returnsUserDetailsWithRole() {
        User user = makeUser(1L, "student", "s@test.com", User.Role.STUDENT);
        when(userRepository.findByEmailIgnoreCase("s@test.com")).thenReturn(Optional.of(user));

        UserDetails details = userService.loadUserByUsername("S@Test.com");

        assertThat(details.getUsername()).isEqualTo("s@test.com");
        assertThat(details.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_STUDENT");
    }

    @Test
    void loadUserByUsername_notFound_throwsUsernameNotFoundException() {
        when(userRepository.findByEmailIgnoreCase("missing@test.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("missing@test.com"));
    }


    @Test
    void createStudent_success_createsWithStudentRoleUnderCallerOrganization() {
        User admin = makeUser(1L, "admin", "admin@test.com", User.Role.ADMIN);
        authenticateAs("admin@test.com", true);
        when(userRepository.findByEmailIgnoreCase("admin@test.com")).thenReturn(Optional.of(admin));
        when(userRepository.existsByEmailIgnoreCase("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password1")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User saved = inv.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 2L);
            return saved;
        });

        UserResponseDTO result = userService.createStudent("newstudent", "new@test.com", "password1");

        assertThat(result.role()).isEqualTo("STUDENT");
        assertThat(result.email()).isEqualTo("new@test.com");
    }

    @Test
    void createStudent_blankUsername_throwsIllegalArgumentException() {
        User admin = makeUser(1L, "admin", "admin@test.com", User.Role.ADMIN);
        authenticateAs("admin@test.com", true);
        when(userRepository.findByEmailIgnoreCase("admin@test.com")).thenReturn(Optional.of(admin));

        assertThrows(IllegalArgumentException.class, () -> userService.createStudent(" ", "new@test.com", "password1"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createStudent_shortPassword_throwsIllegalArgumentException() {
        User admin = makeUser(1L, "admin", "admin@test.com", User.Role.ADMIN);
        authenticateAs("admin@test.com", true);
        when(userRepository.findByEmailIgnoreCase("admin@test.com")).thenReturn(Optional.of(admin));

        assertThrows(IllegalArgumentException.class, () -> userService.createStudent("newstudent", "new@test.com", "short"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createStudent_duplicateEmail_throwsIllegalArgumentException() {
        User admin = makeUser(1L, "admin", "admin@test.com", User.Role.ADMIN);
        authenticateAs("admin@test.com", true);
        when(userRepository.findByEmailIgnoreCase("admin@test.com")).thenReturn(Optional.of(admin));
        when(userRepository.existsByEmailIgnoreCase("new@test.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.createStudent("newstudent", "new@test.com", "password1"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createAdmin_success_createsWithAdminRole() {
        User admin = makeUser(1L, "admin", "admin@test.com", User.Role.ADMIN);
        authenticateAs("admin@test.com", true);
        when(userRepository.findByEmailIgnoreCase("admin@test.com")).thenReturn(Optional.of(admin));
        when(userRepository.existsByEmailIgnoreCase("new-admin@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password1")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponseDTO result = userService.createAdmin("newadmin", "new-admin@test.com", "password1");

        assertThat(result.role()).isEqualTo("ADMIN");
    }

    @Test
    void createCleaner_success_createsWithCleanerRole() {
        User admin = makeUser(1L, "admin", "admin@test.com", User.Role.ADMIN);
        authenticateAs("admin@test.com", true);
        when(userRepository.findByEmailIgnoreCase("admin@test.com")).thenReturn(Optional.of(admin));
        when(userRepository.existsByEmailIgnoreCase("cleaner@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password1")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponseDTO result = userService.createCleaner("cleaner", "cleaner@test.com", "password1");

        assertThat(result.role()).isEqualTo("CLEANER");
    }

    @Test
    void createUserWithRole_invalidRoleString_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.createUserWithRole("x", "x@test.com", "password1", "SUPERUSER"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createFirstAdminForNewOrganization_doesNotRequireAuthenticatedCaller() {
        Organization newOrg = makeOrganization();
        when(userRepository.existsByEmailIgnoreCase("owner@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password1")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponseDTO result = userService.createFirstAdminForNewOrganization("owner", "owner@test.com", "password1", newOrg);

        assertThat(result.role()).isEqualTo("ADMIN");
        verifyNoInteractions(passwordResetTokenRepository);
    }


    @Test
    void seedUserIfMissing_existingUser_returnsExistingWithoutCreating() {
        User existing = makeUser(1L, "admin", "admin@test.com", User.Role.ADMIN);
        when(userRepository.findByEmailIgnoreCase("admin@test.com")).thenReturn(Optional.of(existing));

        UserResponseDTO result = userService.seedUserIfMissing("admin", "admin@test.com", "password1", User.Role.ADMIN);

        assertThat(result.id()).isEqualTo(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void seedUserIfMissing_newUser_createsUnderDefaultOrganization() {
        when(userRepository.findByEmailIgnoreCase("admin@test.com")).thenReturn(Optional.empty());
        Organization defaultOrg = makeOrganization();
        when(organizationRepository.findBySlugIgnoreCase("casacrew")).thenReturn(Optional.of(defaultOrg));
        when(userRepository.existsByEmailIgnoreCase("admin@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password1")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponseDTO result = userService.seedUserIfMissing("admin", "admin@test.com", "password1", User.Role.ADMIN);

        assertThat(result.email()).isEqualTo("admin@test.com");
    }

    @Test
    void seedUserIfMissing_defaultOrganizationMissing_throwsEntityNotFoundException() {
        when(userRepository.findByEmailIgnoreCase("admin@test.com")).thenReturn(Optional.empty());
        when(organizationRepository.findBySlugIgnoreCase("casacrew")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> userService.seedUserIfMissing("admin", "admin@test.com", "password1", User.Role.ADMIN));
    }


    @Test
    void getAllUsers_returnsOrganizationScopedList() {
        User admin = makeUser(1L, "admin", "admin@test.com", User.Role.ADMIN);
        authenticateAs("admin@test.com", true);
        when(userRepository.findByEmailIgnoreCase("admin@test.com")).thenReturn(Optional.of(admin));
        User student = makeUser(2L, "student", "s@test.com", User.Role.STUDENT);
        when(userRepository.findByOrganization_IdOrderByIdAsc(ORG_ID)).thenReturn(List.of(admin, student));

        List<UserResponseDTO> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
    }

    @Test
    void listHousemates_excludesSelfAndReturnsOtherStudentsWithRoom() {
        User me = makeUser(1L, "me", "me@test.com", User.Role.STUDENT);
        authenticateAs("me@test.com", false);
        when(userRepository.findByEmailIgnoreCase("me@test.com")).thenReturn(Optional.of(me));
        User housemate = makeUser(2L, "housemate", "h@test.com", User.Role.STUDENT);
        when(userRepository.findByOrganization_IdAndRole(ORG_ID, User.Role.STUDENT))
                .thenReturn(List.of(me, housemate));
        Room room = new Room("Kamer 2");
        when(roomRepository.findByOccupant_Id(2L)).thenReturn(Optional.of(room));

        List<HousemateDTO> result = userService.listHousemates();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(2L);
        assertThat(result.get(0).roomName()).isEqualTo("Kamer 2");
    }


    @Test
    void getUserById_sameOrganization_returnsDto() {
        User admin = makeUser(1L, "admin", "admin@test.com", User.Role.ADMIN);
        authenticateAs("admin@test.com", true);
        when(userRepository.findByEmailIgnoreCase("admin@test.com")).thenReturn(Optional.of(admin));
        User target = makeUser(2L, "student", "s@test.com", User.Role.STUDENT);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        Optional<UserResponseDTO> result = userService.getUserById(2L);

        assertThat(result).isPresent();
    }

    @Test
    void getUserById_differentOrganization_returnsEmpty() {
        User admin = makeUser(1L, "admin", "admin@test.com", User.Role.ADMIN);
        authenticateAs("admin@test.com", true);
        when(userRepository.findByEmailIgnoreCase("admin@test.com")).thenReturn(Optional.of(admin));
        Organization otherOrg = new Organization("Other", "other");
        ReflectionTestUtils.setField(otherOrg, "id", 99L);
        User target = new User("student", "s@test.com", "hash", User.Role.STUDENT);
        target.setOrganization(otherOrg);
        ReflectionTestUtils.setField(target, "id", 2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        Optional<UserResponseDTO> result = userService.getUserById(2L);

        assertThat(result).isEmpty();
    }

    @Test
    void getUserByEmail_found_returnsDto() {
        User user = makeUser(1L, "student", "s@test.com", User.Role.STUDENT);
        when(userRepository.findByEmailIgnoreCase("s@test.com")).thenReturn(Optional.of(user));

        Optional<UserResponseDTO> result = userService.getUserByEmail("S@Test.com");

        assertThat(result).isPresent();
    }

    @Test
    void getUserByEmail_notFound_returnsEmpty() {
        when(userRepository.findByEmailIgnoreCase("missing@test.com")).thenReturn(Optional.empty());

        assertThat(userService.getUserByEmail("missing@test.com")).isEmpty();
    }


    @Test
    void changeRole_validRole_updatesUsersRole() {
        User admin = makeUser(1L, "admin", "admin@test.com", User.Role.ADMIN);
        authenticateAs("admin@test.com", true);
        when(userRepository.findByEmailIgnoreCase("admin@test.com")).thenReturn(Optional.of(admin));
        User target = makeUser(2L, "student", "s@test.com", User.Role.STUDENT);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        UserResponseDTO result = userService.changeRole(2L, "cleaner");

        assertThat(result.role()).isEqualTo("CLEANER");
    }

    @Test
    void changeRole_invalidRoleString_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> userService.changeRole(2L, "SUPERUSER"));
        verify(userRepository, never()).findById(any());
    }

    @Test
    void changeRole_userNotFoundInOrganization_throwsEntityNotFoundException() {
        User admin = makeUser(1L, "admin", "admin@test.com", User.Role.ADMIN);
        authenticateAs("admin@test.com", true);
        when(userRepository.findByEmailIgnoreCase("admin@test.com")).thenReturn(Optional.of(admin));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.changeRole(99L, "ADMIN"));
    }


    @Test
    void updateProfile_nullDto_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> userService.updateProfile(1L, null));
    }

    @Test
    void updateProfile_ownerUpdatingOwnProfile_succeeds() {
        User me = makeUser(1L, "student", "s@test.com", User.Role.STUDENT);
        authenticateAs("s@test.com", false);
        when(userRepository.findByEmailIgnoreCase("s@test.com")).thenReturn(Optional.of(me));
        when(userRepository.findById(1L)).thenReturn(Optional.of(me));

        UserRequestDTO dto = new UserRequestDTO();
        dto.setFullName("New Name");

        UserResponseDTO result = userService.updateProfile(1L, dto);

        assertThat(result.fullName()).isEqualTo("New Name");
    }

    @Test
    void updateProfile_nonOwnerNonAdmin_throwsAccessDeniedException() {
        User me = makeUser(1L, "student", "s@test.com", User.Role.STUDENT);
        authenticateAs("s@test.com", false);
        when(userRepository.findByEmailIgnoreCase("s@test.com")).thenReturn(Optional.of(me));
        User other = makeUser(2L, "other", "other@test.com", User.Role.STUDENT);
        when(userRepository.findById(2L)).thenReturn(Optional.of(other));

        UserRequestDTO dto = new UserRequestDTO();
        dto.setFullName("Hacked Name");

        assertThrows(AccessDeniedException.class, () -> userService.updateProfile(2L, dto));
    }

    @Test
    void updateProfile_admin_canUpdateOtherUsersProfile() {
        User admin = makeUser(1L, "admin", "admin@test.com", User.Role.ADMIN);
        authenticateAs("admin@test.com", true);
        when(userRepository.findByEmailIgnoreCase("admin@test.com")).thenReturn(Optional.of(admin));
        User student = makeUser(2L, "student", "s@test.com", User.Role.STUDENT);
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));

        UserRequestDTO dto = new UserRequestDTO();
        dto.setFullName("Updated By Admin");

        UserResponseDTO result = userService.updateProfile(2L, dto);

        assertThat(result.fullName()).isEqualTo("Updated By Admin");
    }

    @Test
    void updateProfile_duplicateEmail_throwsIllegalArgumentException() {
        User me = makeUser(1L, "student", "s@test.com", User.Role.STUDENT);
        authenticateAs("s@test.com", false);
        when(userRepository.findByEmailIgnoreCase("s@test.com")).thenReturn(Optional.of(me));
        when(userRepository.findById(1L)).thenReturn(Optional.of(me));
        when(userRepository.existsByEmailIgnoreCase("taken@test.com")).thenReturn(true);

        UserRequestDTO dto = new UserRequestDTO();
        dto.setEmail("taken@test.com");

        assertThrows(IllegalArgumentException.class, () -> userService.updateProfile(1L, dto));
    }


    @Test
    void changeMyPassword_wrongOldPassword_throwsAccessDeniedException() {
        User me = makeUser(1L, "student", "s@test.com", User.Role.STUDENT);
        authenticateAs("s@test.com", false);
        when(userRepository.findByEmailIgnoreCase("s@test.com")).thenReturn(Optional.of(me));
        when(passwordEncoder.matches("wrongOld", "hash")).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> userService.changeMyPassword("wrongOld", "newPassword1"));
        verify(authSessionService, never()).revokeAllForUser(any());
    }

    @Test
    void changeMyPassword_tooShortNewPassword_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> userService.changeMyPassword("oldPassword1", "short"));
    }

    @Test
    void changeMyPassword_success_updatesPasswordAndRevokesAllSessions() {
        User me = makeUser(1L, "student", "s@test.com", User.Role.STUDENT);
        authenticateAs("s@test.com", false);
        when(userRepository.findByEmailIgnoreCase("s@test.com")).thenReturn(Optional.of(me));
        when(passwordEncoder.matches("oldPassword1", "hash")).thenReturn(true);
        when(passwordEncoder.encode("newPassword1")).thenReturn("new-hash");

        userService.changeMyPassword("oldPassword1", "newPassword1");

        assertThat(me.getPassword()).isEqualTo("new-hash");
        verify(authSessionService).revokeAllForUser(me);
    }


    @Test
    void uploadMyProfilePhoto_nullFile_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> userService.uploadMyProfilePhoto(null));
    }

    @Test
    void uploadMyProfilePhoto_disallowedContentType_throwsIllegalArgumentException() {
        MockMultipartFile file = new MockMultipartFile("file", "virus.exe", "application/octet-stream", "data".getBytes());

        assertThrows(IllegalArgumentException.class, () -> userService.uploadMyProfilePhoto(file));
    }

    @Test
    void uploadMyProfilePhoto_validImage_storesFileAndUpdatesProfilePath() {
        User me = makeUser(1L, "student", "s@test.com", User.Role.STUDENT);
        authenticateAs("s@test.com", false);
        when(userRepository.findByEmailIgnoreCase("s@test.com")).thenReturn(Optional.of(me));
        MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", "pngdata".getBytes());

        UserResponseDTO result = userService.uploadMyProfilePhoto(file);

        assertThat(result.profileImagePath()).endsWith(".png");
        assertThat(Files.exists(tempUploadDir.resolve(result.profileImagePath()))).isTrue();
    }


    @Test
    void deleteMyProfilePhoto_removesFileAndClearsPath() throws Exception {
        User me = makeUser(1L, "student", "s@test.com", User.Role.STUDENT);
        authenticateAs("s@test.com", false);
        when(userRepository.findByEmailIgnoreCase("s@test.com")).thenReturn(Optional.of(me));
        Path existing = tempUploadDir.resolve("existing.png");
        Files.writeString(existing, "data");
        me.setProfileImagePath("existing.png");

        UserResponseDTO result = userService.deleteMyProfilePhoto();

        assertThat(result.profileImagePath()).isNull();
        assertThat(Files.exists(existing)).isFalse();
    }


    @Test
    void uploadContract_nonPdf_throwsIllegalArgumentException() {
        MockMultipartFile file = new MockMultipartFile("file", "contract.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "data".getBytes());

        assertThrows(IllegalArgumentException.class, () -> userService.uploadContract(2L, file));
        verify(userRepository, never()).findById(any());
    }

    @Test
    void uploadContract_validPdf_storesFileAndSetsContractFile() {
        User admin = makeUser(1L, "admin", "admin@test.com", User.Role.ADMIN);
        authenticateAs("admin@test.com", true);
        when(userRepository.findByEmailIgnoreCase("admin@test.com")).thenReturn(Optional.of(admin));
        User student = makeUser(2L, "student", "s@test.com", User.Role.STUDENT);
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        MockMultipartFile file = new MockMultipartFile("file", "contract.pdf", "application/pdf", "pdfdata".getBytes());

        UserResponseDTO result = userService.uploadContract(2L, file);

        assertThat(result.contractFile()).endsWith(".pdf");
        assertThat(Files.exists(tempUploadDir.resolve(result.contractFile()))).isTrue();
    }

    @Test
    void deleteContract_clearsContractFile() {
        User admin = makeUser(1L, "admin", "admin@test.com", User.Role.ADMIN);
        authenticateAs("admin@test.com", true);
        when(userRepository.findByEmailIgnoreCase("admin@test.com")).thenReturn(Optional.of(admin));
        User student = makeUser(2L, "student", "s@test.com", User.Role.STUDENT);
        student.setContractFile("contract_2.pdf");
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));

        UserResponseDTO result = userService.deleteContract(2L);

        assertThat(result.contractFile()).isNull();
    }


    @Test
    void resolveContractPath_ownerAccess_returnsPath() {
        User me = makeUser(1L, "student", "s@test.com", User.Role.STUDENT);
        me.setContractFile("contract_1.pdf");
        authenticateAs("s@test.com", false);
        when(userRepository.findByEmailIgnoreCase("s@test.com")).thenReturn(Optional.of(me));
        when(userRepository.findById(1L)).thenReturn(Optional.of(me));

        Path result = userService.resolveContractPath(1L);

        assertThat(result.toString()).endsWith("contract_1.pdf");
    }

    @Test
    void resolveContractPath_nonOwnerNonAdmin_throwsAccessDeniedException() {
        User me = makeUser(1L, "student", "s@test.com", User.Role.STUDENT);
        authenticateAs("s@test.com", false);
        when(userRepository.findByEmailIgnoreCase("s@test.com")).thenReturn(Optional.of(me));
        User other = makeUser(2L, "other", "other@test.com", User.Role.STUDENT);
        other.setContractFile("contract_2.pdf");
        when(userRepository.findById(2L)).thenReturn(Optional.of(other));

        assertThrows(AccessDeniedException.class, () -> userService.resolveContractPath(2L));
    }

    @Test
    void resolveContractPath_noContractOnFile_throwsEntityNotFoundException() {
        User me = makeUser(1L, "student", "s@test.com", User.Role.STUDENT);
        authenticateAs("s@test.com", false);
        when(userRepository.findByEmailIgnoreCase("s@test.com")).thenReturn(Optional.of(me));
        when(userRepository.findById(1L)).thenReturn(Optional.of(me));

        assertThrows(EntityNotFoundException.class, () -> userService.resolveContractPath(1L));
    }


    @Test
    void deleteUser_cascadesRoomInvoicesDocumentsPaymentsSessionsAndReseeds() {
        User admin = makeUser(1L, "admin", "admin@test.com", User.Role.ADMIN);
        authenticateAs("admin@test.com", true);
        when(userRepository.findByEmailIgnoreCase("admin@test.com")).thenReturn(Optional.of(admin));
        User student = makeUser(2L, "student", "s@test.com", User.Role.STUDENT);
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));

        Room room = new Room("Kamer 1");
        room.assignOccupant(student);
        when(roomRepository.findByOccupant_Id(2L)).thenReturn(Optional.of(room));

        Invoice invoice = mock(Invoice.class);
        when(invoiceRepository.findByStudentOrderByIdDesc(student)).thenReturn(List.of(invoice));
        when(documentRepository.findByUploadedByOrderByIdDesc(student)).thenReturn(List.of());
        when(paymentRepository.findByStudent(student)).thenReturn(List.of());

        userService.deleteUser(2L);

        verify(roomRepository).save(room);
        verify(cleaningTaskRepository).unassignAllForUser(student);
        verify(invoiceRepository).deleteAll(List.of(invoice));
        verify(passwordResetTokenRepository).deleteAllByUser(student);
        verify(authSessionService).deleteAllForUser(student);
        verify(userRepository).delete(student);
        verify(cleaningScheduleService).reseedNow();
    }

    @Test
    void deleteUser_reseedThrows_exceptionIsSwallowed() {
        User admin = makeUser(1L, "admin", "admin@test.com", User.Role.ADMIN);
        authenticateAs("admin@test.com", true);
        when(userRepository.findByEmailIgnoreCase("admin@test.com")).thenReturn(Optional.of(admin));
        User student = makeUser(2L, "student", "s@test.com", User.Role.STUDENT);
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(roomRepository.findByOccupant_Id(2L)).thenReturn(Optional.empty());
        when(invoiceRepository.findByStudentOrderByIdDesc(student)).thenReturn(List.of());
        when(documentRepository.findByUploadedByOrderByIdDesc(student)).thenReturn(List.of());
        when(paymentRepository.findByStudent(student)).thenReturn(List.of());
        doThrow(new RuntimeException("boom")).when(cleaningScheduleService).reseedNow();

        assertDoesNotThrow(() -> userService.deleteUser(2L));
        verify(userRepository).delete(student);
    }
}
