package com.casacrew.repository;

import com.casacrew.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByOrganization_IdOrderByExpenseDateDesc(Long organizationId);
}
