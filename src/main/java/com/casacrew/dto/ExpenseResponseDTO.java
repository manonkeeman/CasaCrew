package com.casacrew.dto;

import com.casacrew.model.Expense;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponseDTO(
        Long id,
        String category,
        String description,
        BigDecimal amount,
        LocalDate expenseDate,
        String createdByUsername
) {
    public static ExpenseResponseDTO from(Expense expense) {
        return new ExpenseResponseDTO(
                expense.getId(),
                expense.getCategory(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getExpenseDate(),
                expense.getCreatedBy() != null ? expense.getCreatedBy().getUsername() : null
        );
    }
}
