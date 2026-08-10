package com.villavredestein.service;

import com.villavredestein.dto.InvoiceRequestDTO;
import com.villavredestein.dto.InvoiceResponseDTO;
import com.villavredestein.model.Invoice;
import com.villavredestein.model.Organization;
import com.villavredestein.model.User;
import com.villavredestein.repository.InvoiceRepository;
import com.villavredestein.repository.UserRepository;
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
        Organization organization = new Organization("Villa Vredestein", "villa-vredestein");
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
        stubCurrentOrganizationId();
        when(userService.currentOrganization()).thenReturn(makeOrganization());

        InvoiceRequestDTO dto = new InvoiceRequestDTO();
        dto.setTitle("Huur juli");
        dto.setDescription("Huur kamer 2 juli");
        dto.setAmount(new BigDecimal("500.00"));
        dto.setIssueDate(LocalDate.of(2025, 7, 1));
        dto.setDueDate(LocalDate.of(2025, 7, 31));
        dto.setStudentEmail("student@villavredestein.com");

        User student = makeStudent("student", "student@villavredestein.com");
        when(userRepository.findByEmailIgnoreCase("student@villavredestein.com")).thenReturn(Optional.of(student));

        Invoice saved = withOrg(new Invoice(dto.getTitle(), dto.getDescription(), dto.getAmount(),
                dto.getIssueDate(), dto.getDueDate(), 7, 2025, Invoice.InvoiceStatus.OPEN, student));
        ReflectionTestUtils.setField(saved, "id", 1L);
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(saved);

        InvoiceResponseDTO result = invoiceService.createInvoice(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Huur juli");
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(result.getStudentEmail()).isEqualTo("student@villavredestein.com");
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void createInvoice_studentNotFound_throwsEntityNotFoundException() {
        stubCurrentOrganizationId();
        InvoiceRequestDTO dto = new InvoiceRequestDTO();
        dto.setTitle("Huur juli");
        dto.setAmount(new BigDecimal("500.00"));
        dto.setIssueDate(LocalDate.of(2025, 7, 1));
        dto.setDueDate(LocalDate.of(2025, 7, 31));
        dto.setStudentEmail("onbekend@villavredestein.com");

        when(userRepository.findByEmailIgnoreCase("onbekend@villavredestein.com")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> invoiceService.createInvoice(dto));
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void createInvoice_duplicateInMonth_throwsConflict() {
        stubCurrentOrganizationId();
        when(userService.currentOrganization()).thenReturn(makeOrganization());

        InvoiceRequestDTO dto = new InvoiceRequestDTO();
        dto.setTitle("Huur juli");
        dto.setAmount(new BigDecimal("500.00"));
        dto.setIssueDate(LocalDate.of(2025, 7, 1));
        dto.setDueDate(LocalDate.of(2025, 7, 31));
        dto.setStudentEmail("student@villavredestein.com");

        User student = makeStudent("student", "student@villavredestein.com");
        when(userRepository.findByEmailIgnoreCase("student@villavredestein.com")).thenReturn(Optional.of(student));
        when(invoiceRepository.existsByStudentAndInvoiceMonthAndInvoiceYear(any(User.class), anyInt(), anyInt()))
                .thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> invoiceService.createInvoice(dto));
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(invoiceRepository, never()).save(any());
    }


    @Test
    void getAllInvoices_returnsMappedDtoList() {
        stubCurrentOrganizationId();
        User s1 = makeStudent("student", "student@villavredestein.com");
        User s2 = makeStudent("student2", "student2@villavredestein.com");

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
        User student = makeStudent("student", "student@villavredestein.com");
        Invoice inv = withOrg(new Invoice("Factuur 1", null, new BigDecimal("150.00"),
                LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31), 3, 2025, Invoice.InvoiceStatus.OPEN, student));
        ReflectionTestUtils.setField(inv, "id", 1L);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(inv));

        InvoiceResponseDTO result = invoiceService.getInvoiceById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStudentEmail()).isEqualTo("student@villavredestein.com");
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
        User student = makeStudent("student", "student@villavredestein.com");
        Invoice inv = withOrg(new Invoice("Factuur 1", null, new BigDecimal("100.00"),
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), 1, 2025, Invoice.InvoiceStatus.OPEN, student));
        ReflectionTestUtils.setField(inv, "id", 1L);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(inv));

        InvoiceResponseDTO result = invoiceService.getInvoiceByIdForCaller(1L, "admin@villavredestein.com", true);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getInvoiceByIdForCaller_student_canAccessOwnInvoice() {
        stubCurrentOrganizationId();
        User student = makeStudent("student", "student@villavredestein.com");
        Invoice inv = withOrg(new Invoice("Factuur 1", null, new BigDecimal("100.00"),
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), 1, 2025, Invoice.InvoiceStatus.OPEN, student));
        ReflectionTestUtils.setField(inv, "id", 1L);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(inv));

        InvoiceResponseDTO result = invoiceService.getInvoiceByIdForCaller(1L, "student@villavredestein.com", false);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getInvoiceByIdForCaller_student_cannotAccessOtherInvoice_throwsAccessDenied() {
        stubCurrentOrganizationId();
        User student = makeStudent("student", "student@villavredestein.com");
        Invoice inv = withOrg(new Invoice("Factuur 1", null, new BigDecimal("100.00"),
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), 1, 2025, Invoice.InvoiceStatus.OPEN, student));
        ReflectionTestUtils.setField(inv, "id", 1L);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(inv));

        assertThrows(AccessDeniedException.class,
                () -> invoiceService.getInvoiceByIdForCaller(1L, "ander@villavredestein.com", false));
    }


    @Test
    void getInvoicesForStudent_withValidEmail_returnsDtoList() {
        User student = makeStudent("student", "student@villavredestein.com");
        Invoice inv = withOrg(new Invoice("Factuur 1", null, new BigDecimal("100.00"),
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), 1, 2025, Invoice.InvoiceStatus.OPEN, student));
        ReflectionTestUtils.setField(inv, "id", 1L);
        when(invoiceRepository.findByStudent_EmailIgnoreCaseOrderByIdDesc("student@villavredestein.com"))
                .thenReturn(List.of(inv));

        List<InvoiceResponseDTO> result = invoiceService.getInvoicesForStudent("student@villavredestein.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStudentEmail()).isEqualTo("student@villavredestein.com");
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
        User student = makeStudent("student", "student@villavredestein.com");
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
        User student = makeStudent("student", "student@villavredestein.com");
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
        User student = makeStudent("student", "student@villavredestein.com");
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
        User student = makeStudent("student", "student@villavredestein.com");
        Invoice inv = withOrg(new Invoice("Factuur 1", null, new BigDecimal("100.00"),
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), 1, 2025, Invoice.InvoiceStatus.OPEN, student));

        assertThrows(IllegalArgumentException.class, () -> invoiceService.saveReminderMeta(inv));
    }


    @Test
    void getAllOpenInvoices_returnsOnlyOpenInvoices() {
        User student = makeStudent("student", "student@villavredestein.com");
        Invoice open1 = withOrg(new Invoice("Inv1", null, new BigDecimal("10.00"),
                LocalDate.now(), LocalDate.now().plusDays(10),
                LocalDate.now().getMonthValue(), LocalDate.now().getYear(), Invoice.InvoiceStatus.OPEN, student));
        Invoice open2 = withOrg(new Invoice("Inv2", null, new BigDecimal("20.00"),
                LocalDate.now(), LocalDate.now().plusDays(20),
                LocalDate.now().getMonthValue(), LocalDate.now().getYear(), Invoice.InvoiceStatus.OPEN, student));
        when(invoiceRepository.findByStatusOrderByIdDesc(Invoice.InvoiceStatus.OPEN))
                .thenReturn(List.of(open2, open1));

        List<Invoice> result = invoiceService.getAllOpenInvoices();

        assertThat(result).allMatch(i -> i.getStatus() == Invoice.InvoiceStatus.OPEN);
        verify(invoiceRepository).findByStatusOrderByIdDesc(Invoice.InvoiceStatus.OPEN);
    }


    @Test
    void getUpcomingInvoices_returnsUpcomingOpenInvoices() {
        User student = makeStudent("student", "student@villavredestein.com");
        Invoice upcoming = withOrg(new Invoice("Soon", null, new BigDecimal("10.00"),
                LocalDate.now(), LocalDate.now().plusDays(3),
                LocalDate.now().getMonthValue(), LocalDate.now().getYear(), Invoice.InvoiceStatus.OPEN, student));
        when(invoiceRepository.findByStatusAndDueDateBetweenOrderByDueDateAsc(
                eq(Invoice.InvoiceStatus.OPEN), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(upcoming));

        List<Invoice> result = invoiceService.getUpcomingInvoices(4);

        assertThat(result).containsExactly(upcoming);
        verify(invoiceRepository).findByStatusAndDueDateBetweenOrderByDueDateAsc(
                eq(Invoice.InvoiceStatus.OPEN), any(LocalDate.class), any(LocalDate.class));
    }
}
