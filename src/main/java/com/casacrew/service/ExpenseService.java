package com.casacrew.service;

import com.casacrew.dto.ExpenseRequestDTO;
import com.casacrew.dto.ExpenseResponseDTO;
import com.casacrew.model.Expense;
import com.casacrew.repository.ExpenseRepository;
import com.casacrew.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ExpensePdfService expensePdfService;

    public ExpenseService(ExpenseRepository expenseRepository, UserRepository userRepository, UserService userService,
                           ExpensePdfService expensePdfService) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.expensePdfService = expensePdfService;
    }

    public ExpenseResponseDTO create(ExpenseRequestDTO dto) {
        Expense expense = new Expense();
        expense.setOrganization(userService.currentOrganization());
        expense.setCreatedBy(userRepository.getReferenceById(userService.getMyId()));
        expense.setCategory(dto.category());
        expense.setDescription(dto.description());
        expense.setAmount(dto.amount());
        expense.setExpenseDate(dto.expenseDate());
        return ExpenseResponseDTO.from(expenseRepository.save(expense));
    }

    public ExpenseResponseDTO update(Long id, ExpenseRequestDTO dto) {
        Expense expense = findInCurrentOrganization(id);
        expense.setCategory(dto.category());
        expense.setDescription(dto.description());
        expense.setAmount(dto.amount());
        expense.setExpenseDate(dto.expenseDate());
        return ExpenseResponseDTO.from(expenseRepository.save(expense));
    }

    public void delete(Long id) {
        expenseRepository.delete(findInCurrentOrganization(id));
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponseDTO> list() {
        return expenseRepository.findByOrganization_IdOrderByExpenseDateDesc(userService.currentOrganizationId())
                .stream().map(ExpenseResponseDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public byte[] exportCsv() {
        List<Expense> expenses = expenseRepository.findByOrganization_IdOrderByExpenseDateDesc(userService.currentOrganizationId());

        StringBuilder csv = new StringBuilder();
        csv.append("Datum;Categorie;Omschrijving;Bedrag\n");
        for (Expense e : expenses) {
            csv.append(e.getExpenseDate()).append(';')
                    .append(e.getCategory()).append(';')
                    .append(escape(e.getDescription())).append(';')
                    .append(e.getAmount()).append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] exportPdf() {
        List<Expense> expenses = expenseRepository.findByOrganization_IdOrderByExpenseDateDesc(userService.currentOrganizationId());
        return expensePdfService.generate(expenses, userService.currentOrganization().getName());
    }

    private String escape(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(";") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private Expense findInCurrentOrganization(Long id) {
        Long organizationId = userService.currentOrganizationId();
        return expenseRepository.findById(id)
                .filter(e -> e.getOrganization().getId().equals(organizationId))
                .orElseThrow(() -> new EntityNotFoundException("Uitgave niet gevonden: " + id));
    }
}
