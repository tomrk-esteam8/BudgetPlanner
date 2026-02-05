package com.budget.application.service;

import com.budget.domain.*;

import java.time.LocalDate;
import java.util.List;

public interface MonthlySummaryService {

    MonthlySummary calculate(
            AccountingMonth month,
            MonthlyFunds funds,
            MonthlySavings savings,
            List<CyclicExpense> cyclicExpenses,
            List<Expense> expenses
    );

    /**
     * Calculate monthly summary with daily limit based on remaining days from a specific date.
     * If date is null, uses today's date.
     */
    MonthlySummary calculate(
            AccountingMonth month,
            MonthlyFunds funds,
            MonthlySavings savings,
            List<CyclicExpense> cyclicExpenses,
            List<Expense> expenses,
            LocalDate requestDate
    );
}
