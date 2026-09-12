package com.casacrew.service;

import com.casacrew.dto.ExpenseRequestDTO;
import com.casacrew.dto.ExpenseResponseDTO;
import com.casacrew.model.Expense;
import com.casacrew.model.Organization;
import com.casacrew.model.User;
import com.casacrew.repository.ExpenseRepository;
import com.casacrew.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    private static final Long ORG_ID = 1L;
    private static final Long OTHER_ORG_ID = 2L;

    @Mock ExpenseRepository expenseRepository;
    @Mock UserRepository userRepository;
    @Mock UserService userService;
    @Mock ExpensePdfService expensePdfService;
    @InjectMocks ExpenseService expenseService;

    private void stubCurrentOrganizationId() {
        when(userService.currentOrganizationId()).thenReturn(ORG_ID);
    }

    private Organization makeOrganization(long id) {
        Organization organization = new Organization("CasaCrew", "casacrew");
        ReflectionTestUtils.setField(organization, "id", id);
        return organization;
    }

    private User makeUser(long id, String username) {
        User user = new User(username, username + "@test.com", "hash", User.Role.ADMIN);
        user.setOrganization(makeOrganization(ORG_ID));
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Expense makeExpense(long id, Organization organization, String category, String description,
                                 BigDecimal amount, LocalDate date, User createdBy) {
        Expense expense = new Expense();
        ReflectionTestUtils.setField(expense, "id", id);
        expense.setOrganization(organization);
        expense.setCategory(category);
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setExpenseDate(date);
        expense.setCreatedBy(createdBy);
        return expense;
    }


    @Test
    void create_success_returnsDto() {
        User admin = makeUser(1L, "admin");
        when(userService.currentOrganization()).thenReturn(makeOrganization(ORG_ID));
        when(userService.getMyId()).thenReturn(1L);
        when(userRepository.getReferenceById(1L)).thenReturn(admin);
        when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> {
            Expense e = inv.getArgument(0);
            ReflectionTestUtils.setField(e, "id", 10L);
            return e;
        });

        ExpenseRequestDTO dto = new ExpenseRequestDTO("ONDERHOUD", "Nieuwe lamp", new BigDecimal("25.00"), LocalDate.of(2025, 3, 1));
        ExpenseResponseDTO result = expenseService.create(dto);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.category()).isEqualTo("ONDERHOUD");
        assertThat(result.description()).isEqualTo("Nieuwe lamp");
        assertThat(result.amount()).isEqualByComparingTo("25.00");
        assertThat(result.createdByUsername()).isEqualTo("admin");
    }


    @Test
    void update_success_updatesAndReturnsDto() {
        stubCurrentOrganizationId();
        Expense existing = makeExpense(1L, makeOrganization(ORG_ID), "ONDERHOUD", "Oud", new BigDecimal("10.00"),
                LocalDate.of(2025, 1, 1), null);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        ExpenseRequestDTO dto = new ExpenseRequestDTO("REPARATIE", "Nieuw", new BigDecimal("50.00"), LocalDate.of(2025, 2, 2));
        ExpenseResponseDTO result = expenseService.update(1L, dto);

        assertThat(result.category()).isEqualTo("REPARATIE");
        assertThat(result.description()).isEqualTo("Nieuw");
        assertThat(result.amount()).isEqualByComparingTo("50.00");
        assertThat(result.expenseDate()).isEqualTo(LocalDate.of(2025, 2, 2));
    }

    @Test
    void update_notFound_throwsEntityNotFoundException() {
        stubCurrentOrganizationId();
        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

        ExpenseRequestDTO dto = new ExpenseRequestDTO("OVERIG", "x", BigDecimal.ONE, LocalDate.now());
        assertThrows(EntityNotFoundException.class, () -> expenseService.update(99L, dto));
        verify(expenseRepository, never()).save(any());
    }

    @Test
    void update_belongsToDifferentOrganization_throwsEntityNotFoundException() {
        stubCurrentOrganizationId();
        Expense foreign = makeExpense(1L, makeOrganization(OTHER_ORG_ID), "ONDERHOUD", "Andere org",
                new BigDecimal("10.00"), LocalDate.of(2025, 1, 1), null);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(foreign));

        ExpenseRequestDTO dto = new ExpenseRequestDTO("OVERIG", "x", BigDecimal.ONE, LocalDate.now());
        assertThrows(EntityNotFoundException.class, () -> expenseService.update(1L, dto));
        verify(expenseRepository, never()).save(any());
    }


    @Test
    void delete_success_deletesExpense() {
        stubCurrentOrganizationId();
        Expense existing = makeExpense(1L, makeOrganization(ORG_ID), "ONDERHOUD", "x",
                new BigDecimal("10.00"), LocalDate.of(2025, 1, 1), null);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertDoesNotThrow(() -> expenseService.delete(1L));
        verify(expenseRepository).delete(existing);
    }

    @Test
    void delete_notFound_throwsEntityNotFoundException() {
        stubCurrentOrganizationId();
        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> expenseService.delete(99L));
        verify(expenseRepository, never()).delete(any());
    }

    @Test
    void delete_belongsToDifferentOrganization_throwsEntityNotFoundException() {
        stubCurrentOrganizationId();
        Expense foreign = makeExpense(1L, makeOrganization(OTHER_ORG_ID), "ONDERHOUD", "x",
                new BigDecimal("10.00"), LocalDate.of(2025, 1, 1), null);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(foreign));

        assertThrows(EntityNotFoundException.class, () -> expenseService.delete(1L));
        verify(expenseRepository, never()).delete(any());
    }


    @Test
    void list_returnsMappedDtoList() {
        stubCurrentOrganizationId();
        Expense e1 = makeExpense(1L, makeOrganization(ORG_ID), "ONDERHOUD", "A", new BigDecimal("10.00"), LocalDate.of(2025, 2, 1), null);
        Expense e2 = makeExpense(2L, makeOrganization(ORG_ID), "REPARATIE", "B", new BigDecimal("20.00"), LocalDate.of(2025, 1, 1), null);
        when(expenseRepository.findByOrganization_IdOrderByExpenseDateDesc(ORG_ID)).thenReturn(List.of(e1, e2));

        List<ExpenseResponseDTO> result = expenseService.list();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(1L);
    }

    @Test
    void list_empty_returnsEmptyList() {
        stubCurrentOrganizationId();
        when(expenseRepository.findByOrganization_IdOrderByExpenseDateDesc(ORG_ID)).thenReturn(List.of());

        assertThat(expenseService.list()).isEmpty();
    }


    @Test
    void exportCsv_containsHeaderAndRows() {
        stubCurrentOrganizationId();
        Expense e1 = makeExpense(1L, makeOrganization(ORG_ID), "ONDERHOUD", "Simpel", new BigDecimal("10.00"), LocalDate.of(2025, 2, 1), null);
        when(expenseRepository.findByOrganization_IdOrderByExpenseDateDesc(ORG_ID)).thenReturn(List.of(e1));

        byte[] csvBytes = expenseService.exportCsv();
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        assertThat(csv).startsWith("Datum;Categorie;Omschrijving;Bedrag\n");
        assertThat(csv).contains("2025-02-01;ONDERHOUD;Simpel;10.00");
    }

    @Test
    void exportCsv_escapesDescriptionContainingSemicolon() {
        stubCurrentOrganizationId();
        Expense e1 = makeExpense(1L, makeOrganization(ORG_ID), "ONDERHOUD", "Met; puntkomma",
                new BigDecimal("10.00"), LocalDate.of(2025, 2, 1), null);
        when(expenseRepository.findByOrganization_IdOrderByExpenseDateDesc(ORG_ID)).thenReturn(List.of(e1));

        String csv = new String(expenseService.exportCsv(), StandardCharsets.UTF_8);

        assertThat(csv).contains("\"Met; puntkomma\"");
    }

    @Test
    void exportCsv_empty_returnsOnlyHeader() {
        stubCurrentOrganizationId();
        when(expenseRepository.findByOrganization_IdOrderByExpenseDateDesc(ORG_ID)).thenReturn(List.of());

        String csv = new String(expenseService.exportCsv(), StandardCharsets.UTF_8);

        assertThat(csv).isEqualTo("Datum;Categorie;Omschrijving;Bedrag\n");
    }


    @Test
    void exportPdf_delegatesToExpensePdfServiceWithOrganizationName() {
        stubCurrentOrganizationId();
        Organization org = makeOrganization(ORG_ID);
        when(userService.currentOrganization()).thenReturn(org);
        Expense e1 = makeExpense(1L, org, "ONDERHOUD", "A", new BigDecimal("10.00"), LocalDate.of(2025, 2, 1), null);
        List<Expense> expenses = List.of(e1);
        when(expenseRepository.findByOrganization_IdOrderByExpenseDateDesc(ORG_ID)).thenReturn(expenses);
        byte[] fakePdf = new byte[]{1, 2, 3};
        when(expensePdfService.generate(expenses, "CasaCrew")).thenReturn(fakePdf);

        byte[] result = expenseService.exportPdf();

        assertThat(result).isEqualTo(fakePdf);
        verify(expensePdfService).generate(expenses, "CasaCrew");
    }
}
