package com.casacrew.service;

import com.casacrew.dto.InvoiceRequestDTO;
import com.casacrew.dto.InvoiceResponseDTO;
import com.casacrew.model.Invoice;
import com.casacrew.model.Organization;
import com.casacrew.model.User;
import com.casacrew.repository.InvoiceRepository;
import com.casacrew.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    private static final Long ORG_ID = 1L;

    @Mock InvoiceRepository invoiceRepository;
    @Mock UserRepository userRepository;
    @Mock InvoicePdfService invoicePdfService;
    @Mock UserService userService;
    @InjectMocks InvoiceService invoiceService;

    private Organization makeOrganization() {
        Organization organization = new Organization("CasaCrew", "casacrew");
        ReflectionTestUtils.setField(organization, "id", ORG_ID);
        return organization;
    }

    private User makeStudent(String username, String email) {
        User user = new User(username, email, "hash", User.Role.STUDENT);
        user.setOrganization(makeOrganization());
        return user;
    }

    private Invoice withOrg(Invoice invoice) {
        invoice.setOrganization(makeOrganization());
        return invoice;
    }

    private void stubCurrentOrganizationId() {
        when(userService.currentOrganizationId()).thenReturn(ORG_ID);
    }


    @Test
    void createInvoice_withExistingStudent_savesAndReturnsDto() {
        when(userService.currentOrganization()).thenReturn(makeOrganization());

        InvoiceRequestDTO dto = new InvoiceRequestDTO();
        dto.setTitle("Huur juli");
        dto.setDescription("Huur kamer 2 juli");
        dto.setAmount(new BigDecimal("500.00"));
        dto.setIssueDate(LocalDate.of(2025, 7, 1));
        dto.setDueDate(LocalDate.of(2025, 7, 31));
        dto.setStudentEmail("student@casacrew.nl");

        User student = makeStudent("student", "student@casacrew.nl");
        when(userRepository.findByEmailIgnoreCase("student@casacrew.nl")).thenReturn(Optional.of(student));

        Invoice saved = withOrg(new Invoice(dto.getTitle(), dto.getDescription(), dto.getAmount(),
                dto.getIssueDate(), dto.getDueDate(), 7, 2025, Invoice.InvoiceStatus.OPEN, student));
        ReflectionTestUtils.setField(saved, "id", 1L);
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(saved);

        InvoiceResponseDTO result = invoiceService.createInvoice(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Huur juli");
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(result.getStudentEmail()).isEqualTo("student@casacrew.nl");
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void createInvoice_studentNotFound_throwsEntityNotFoundException() {
        when(userService.currentOrganization()).thenReturn(makeOrganization());
        InvoiceRequestDTO dto = new InvoiceRequestDTO();
        dto.setTitle("Huur juli");
        dto.setAmount(new BigDecimal("500.00"));
        dto.setIssueDate(LocalDate.of(2025, 7, 1));
        dto.setDueDate(LocalDate.of(2025, 7, 31));
        dto.setStudentEmail("onbekend@casacrew.nl");

        when(userRepository.findByEmailIgnoreCase("onbekend@casacrew.nl")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> invoiceService.createInvoice(dto));
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void createInvoice_duplicateInMonth_throwsConflict() {
        when(userService.currentOrganization()).thenReturn(makeOrganization());

        InvoiceRequestDTO dto = new InvoiceRequestDTO();
        dto.setTitle("Huur juli");
        dto.setAmount(new BigDecimal("500.00"));
        dto.setIssueDate(LocalDate.of(2025, 7, 1));
        dto.setDueDate(LocalDate.of(2025, 7, 31));
        dto.setStudentEmail("student@casacrew.nl");

        User student = makeStudent("student", "student@casacrew.nl");
        when(userRepository.findByEmailIgnoreCase("student@casacrew.nl")).thenReturn(Optional.of(student));
        when(invoiceRepository.existsByOrganization_IdAndStudentAndInvoiceMonthAndInvoiceYear(
                eq(ORG_ID), any(User.class), anyInt(), anyInt()))
                .thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> invoiceService.createInvoice(dto));
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(invoiceRepository, never()).save(any());
    }


    @Test
    void getAllInvoices_returnsMappedDtoList() {
        stubCurrentOrganizationId();
        User s1 = makeStudent("student", "student@casacrew.nl");
        User s2 = makeStudent("student2", "student2@casacrew.nl");

        Invoice inv1 = withOrg(new Invoice("Factuur 1", null, new BigDecimal("100.00"),
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), 1, 2025, Invoice.InvoiceStatus.OPEN, s1));
        ReflectionTestUtils.setField(inv1, "id", 1L);

        Invoice inv2 = withOrg(new Invoice("Factuur 2", null, new BigDecimal("200.00"),
                LocalDate.of(2025, 2, 1), LocalDate.of(2025, 2, 28), 2, 2025, Invoice.InvoiceStatus.PAID, s2));
        ReflectionTestUtils.setField(inv2, "id", 2L);

        when(invoiceRepository.findByOrganization_IdOrderByIdDesc(ORG_ID)).thenReturn(List.of(inv2, inv1));

        List<InvoiceResponseDTO> result = invoiceService.getAllInvoices();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(2L);
        assertThat(result.get(1).getId()).isEqualTo(1L);
    }


    @Test
    void getInvoiceById_existing_returnsDto() {
        stubCurrentOrganizationId();
        User student = makeStudent("student", "student@casacrew.nl");
        Invoice inv = withOrg(new Invoice("Factuur 1", null, new BigDecimal("150.00"),
                LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31), 3, 2025, Invoice.InvoiceStatus.OPEN, student));
        ReflectionTestUtils.setField(inv, "id", 1L);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(inv));

        InvoiceResponseDTO result = invoiceService.getInvoiceById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStudentEmail()).isEqualTo("student@casacrew.nl");
    }

    @Test
    void getInvoiceById_notExisting_throwsEntityNotFoundException() {
        stubCurrentOrganizationId();
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> invoiceService.getInvoiceById(99L));
    }


    @Test
    void getInvoiceByIdForCaller_admin_canAccessAnyInvoice() {
        stubCurrentOrganizationId();
        User student = makeStudent("student", "student@casacrew.nl");
        Invoice inv = withOrg(new Invoice("Factuur 1", null, new BigDecimal("100.00"),
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), 1, 2025, Invoice.InvoiceStatus.OPEN, student));
        ReflectionTestUtils.setField(inv, "id", 1L);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(inv));

        InvoiceResponseDTO result = invoiceService.getInvoiceByIdForCaller(1L, "admin@casacrew.nl", true);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getInvoiceByIdForCaller_student_canAccessOwnInvoice() {
        stubCurrentOrganizationId();
        User student = makeStudent("student", "student@casacrew.nl");
        Invoice inv = withOrg(new Invoice("Factuur 1", null, new BigDecimal("100.00"),
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), 1, 2025, Invoice.InvoiceStatus.OPEN, student));
        ReflectionTestUtils.setField(inv, "id", 1L);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(inv));

        InvoiceResponseDTO result = invoiceService.getInvoiceByIdForCaller(1L, "student@casacrew.nl", false);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getInvoiceByIdForCaller_student_cannotAccessOtherInvoice_throwsAccessDenied() {
        stubCurrentOrganizationId();
        User student = makeStudent("student", "student@casacrew.nl");
        Invoice inv = withOrg(new Invoice("Factuur 1", null, new BigDecimal("100.00"),
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), 1, 2025, Invoice.InvoiceStatus.OPEN, student));
        ReflectionTestUtils.setField(inv, "id", 1L);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(inv));

        assertThrows(AccessDeniedException.class,
                () -> invoiceService.getInvoiceByIdForCaller(1L, "ander@casacrew.nl", false));
    }


    @Test
    void getInvoicesForStudent_withValidEmail_returnsDtoList() {
        User student = makeStudent("student", "student@casacrew.nl");
        Invoice inv = withOrg(new Invoice("Factuur 1", null, new BigDecimal("100.00"),
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), 1, 2025, Invoice.InvoiceStatus.OPEN, student));
        ReflectionTestUtils.setField(inv, "id", 1L);
        when(invoiceRepository.findByStudent_EmailIgnoreCaseOrderByIdDesc("student@casacrew.nl"))
                .thenReturn(List.of(inv));

        List<InvoiceResponseDTO> result = invoiceService.getInvoicesForStudent("student@casacrew.nl");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStudentEmail()).isEqualTo("student@casacrew.nl");
    }

    @Test
    void getInvoicesForStudent_withNullEmail_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> invoiceService.getInvoicesForStudent(null));
    }

    @Test
    void getInvoicesForStudent_withBlankEmail_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> invoiceService.getInvoicesForStudent("   "));
    }


    @Test
    void updateStatus_existing_updatesAndReturnsDto() {
        stubCurrentOrganizationId();
        User student = makeStudent("student", "student@casacrew.nl");
        Invoice inv = withOrg(new Invoice("Factuur 1", null, new BigDecimal("150.00"),
                LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31), 3, 2025, Invoice.InvoiceStatus.OPEN, student));
        ReflectionTestUtils.setField(inv, "id", 1L);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(inv));

        InvoiceResponseDTO result = invoiceService.updateStatus(1L, "paid");

        assertThat(result.getStatus()).isEqualTo("PAID");
    }

    @Test
    void updateStatus_notExisting_throwsEntityNotFoundException() {
        stubCurrentOrganizationId();
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> invoiceService.updateStatus(99L, "paid"));
    }

    @Test
    void updateStatus_withNullStatus_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> invoiceService.updateStatus(1L, (String) null));
    }

    @Test
    void updateStatus_withInvalidStatus_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> invoiceService.updateStatus(1L, "ONBEKEND"));
    }


    @Test
    void deleteInvoice_existing_deletes() {
        stubCurrentOrganizationId();
        User student = makeStudent("student", "student@casacrew.nl");
        Invoice inv = withOrg(new Invoice("Factuur 1", null, new BigDecimal("150.00"),
                LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31), 3, 2025, Invoice.InvoiceStatus.OPEN, student));
        ReflectionTestUtils.setField(inv, "id", 1L);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(inv));

        assertDoesNotThrow(() -> invoiceService.deleteInvoice(1L));
        verify(invoiceRepository).delete(inv);
    }

    @Test
    void deleteInvoice_notExisting_throwsEntityNotFoundException() {
        stubCurrentOrganizationId();
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> invoiceService.deleteInvoice(99L));
        verify(invoiceRepository, never()).deleteById(any());
    }


    @Test
    void saveReminderMeta_validInvoice_saves() {
        User student = makeStudent("student", "student@casacrew.nl");
        Invoice inv = withOrg(new Invoice("Factuur 1", null, new BigDecimal("100.00"),
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), 1, 2025, Invoice.InvoiceStatus.OPEN, student));
        ReflectionTestUtils.setField(inv, "id", 1L);
        when(invoiceRepository.save(inv)).thenReturn(inv);

        invoiceService.saveReminderMeta(inv);

        verify(invoiceRepository).save(inv);
    }

    @Test
    void saveReminderMeta_nullInvoice_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> invoiceService.saveReminderMeta(null));
    }

    @Test
    void saveReminderMeta_invoiceWithoutId_throwsIllegalArgumentException() {
        User student = makeStudent("student", "student@casacrew.nl");
        Invoice inv = withOrg(new Invoice("Factuur 1", null, new BigDecimal("100.00"),
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), 1, 2025, Invoice.InvoiceStatus.OPEN, student));

        assertThrows(IllegalArgumentException.class, () -> invoiceService.saveReminderMeta(inv));
    }


    @Test
    void getAllOpenInvoices_returnsOnlyOpenInvoices() {
        User student = makeStudent("student", "student@casacrew.nl");
        Invoice open1 = withOrg(new Invoice("Inv1", null, new BigDecimal("10.00"),
                LocalDate.now(), LocalDate.now().plusDays(10),
                LocalDate.now().getMonthValue(), LocalDate.now().getYear(), Invoice.InvoiceStatus.OPEN, student));
        Invoice open2 = withOrg(new Invoice("Inv2", null, new BigDecimal("20.00"),
                LocalDate.now(), LocalDate.now().plusDays(20),
                LocalDate.now().getMonthValue(), LocalDate.now().getYear(), Invoice.InvoiceStatus.OPEN, student));
        when(invoiceRepository.findByOrganization_IdAndStatusOrderByIdDesc(ORG_ID, Invoice.InvoiceStatus.OPEN))
                .thenReturn(List.of(open2, open1));

        List<Invoice> result = invoiceService.getAllOpenInvoices(ORG_ID);

        assertThat(result).allMatch(i -> i.getStatus() == Invoice.InvoiceStatus.OPEN);
        verify(invoiceRepository).findByOrganization_IdAndStatusOrderByIdDesc(ORG_ID, Invoice.InvoiceStatus.OPEN);
    }


    @Test
    void getUpcomingInvoices_returnsUpcomingOpenInvoices() {
        User student = makeStudent("student", "student@casacrew.nl");
        Invoice upcoming = withOrg(new Invoice("Soon", null, new BigDecimal("10.00"),
                LocalDate.now(), LocalDate.now().plusDays(3),
                LocalDate.now().getMonthValue(), LocalDate.now().getYear(), Invoice.InvoiceStatus.OPEN, student));
        when(invoiceRepository.findByOrganization_IdAndStatusAndDueDateBetweenOrderByDueDateAsc(
                eq(ORG_ID), eq(Invoice.InvoiceStatus.OPEN), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(upcoming));

        List<Invoice> result = invoiceService.getUpcomingInvoices(ORG_ID, 4);

        assertThat(result).containsExactly(upcoming);
        verify(invoiceRepository).findByOrganization_IdAndStatusAndDueDateBetweenOrderByDueDateAsc(
                eq(ORG_ID), eq(Invoice.InvoiceStatus.OPEN), any(LocalDate.class), any(LocalDate.class));
    }
}
