package com.budget.infrastructure.repository;

import com.budget.domain.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findBySpentAtBetween(LocalDate startDate, LocalDate endDate);
    List<Expense> findByCategory(String category);
}
