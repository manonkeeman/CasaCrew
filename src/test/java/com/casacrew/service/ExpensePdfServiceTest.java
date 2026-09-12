package com.casacrew.service;

import com.casacrew.model.Expense;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ExpensePdfServiceTest {

    private final ExpensePdfService service = new ExpensePdfService();

    private Expense makeExpense(long id, String category, String description, BigDecimal amount, LocalDate date) {
        Expense expense = new Expense();
        ReflectionTestUtils.setField(expense, "id", id);
        expense.setCategory(category);
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setExpenseDate(date);
        return expense;
    }

    @Test
    void generate_withExpenses_returnsNonEmptyPdfBytes() {
        List<Expense> expenses = List.of(
                makeExpense(1L, "ONDERHOUD", "Lamp", new BigDecimal("25.00"), LocalDate.of(2025, 3, 1)),
                makeExpense(2L, "REPARATIE", "Slot", new BigDecimal("40.00"), LocalDate.of(2025, 3, 5))
        );

        byte[] pdf = service.generate(expenses, "CasaCrew");

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF");
    }

    @Test
    void generate_withEmptyList_returnsNonEmptyPdfBytes() {
        byte[] pdf = service.generate(List.of(), "CasaCrew");

        assertThat(pdf).isNotEmpty();
    }

    @Test
    void generate_withNullOrganizationName_doesNotThrow() {
        List<Expense> expenses = List.of(makeExpense(1L, "OVERIG", "x", new BigDecimal("1.00"), LocalDate.now()));

        byte[] pdf = assertDoesNotThrow(() -> service.generate(expenses, null));

        assertThat(pdf).isNotEmpty();
    }

    @Test
    void generate_expenseWithNullDescription_doesNotThrow() {
        List<Expense> expenses = List.of(makeExpense(1L, "OVERIG", null, new BigDecimal("1.00"), LocalDate.now()));

        byte[] pdf = assertDoesNotThrow(() -> service.generate(expenses, "CasaCrew"));

        assertThat(pdf).isNotEmpty();
    }
}
