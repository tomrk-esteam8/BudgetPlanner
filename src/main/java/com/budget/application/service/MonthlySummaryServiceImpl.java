package com.budget.application.service;

import com.budget.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MonthlySummaryServiceImpl implements MonthlySummaryService {

    private final CyclicExpenseCalculator cyclicExpenseCalculator;
    private final DailyLimitCalculator dailyLimitCalculator;

    @Override
    public MonthlySummary calculate(
            AccountingMonth month,
            MonthlyFunds funds,
            MonthlySavings savings,
            List<CyclicExpense> cyclicExpenses,
            List<Expense> expenses) {
        return calculate(month, funds, savings, cyclicExpenses, expenses, LocalDate.now());
    }

    @Override
    public MonthlySummary calculate(
            AccountingMonth month,
            MonthlyFunds funds,
            MonthlySavings savings,
            List<CyclicExpense> cyclicExpenses,
            List<Expense> expenses,
            LocalDate requestDate) {

        BigDecimal fundsAmount = funds != null ? funds.getAmount() : BigDecimal.ZERO;
        BigDecimal savingsAmount = savings != null ? savings.getAmount() : BigDecimal.ZERO;

        LocalDate dateForCalculation = requestDate != null ? requestDate : LocalDate.now();
        BigDecimal fixedCosts = calculateFixedCosts(cyclicExpenses, month);
        BigDecimal spent = calculateSpent(expenses, month, dateForCalculation);
        // available = funds - savings - fixedCosts - spent
        // Use requestDate to calculate daily limit based on remaining days
        BigDecimal available = fundsAmount
                .subtract(savingsAmount)
                .subtract(fixedCosts)
                .subtract(spent);

        BigDecimal dailyLimit = dailyLimitCalculator.calculateFromDate(available, dateForCalculation);

        return MonthlySummary.builder()
                .date(dateForCalculation)
                .funds(fundsAmount)
                .savings(savingsAmount)
                .fixedCosts(fixedCosts)
                .spent(spent)
                .available(available)
                .dailyLimit(dailyLimit)
                .build();
    }

    private BigDecimal calculateFixedCosts(List<CyclicExpense> cyclicExpenses, AccountingMonth month) {
        return cyclicExpenses.stream()
                .filter(expense -> cyclicExpenseCalculator.applies(expense, month))
                .map(expense -> cyclicExpenseCalculator.amountForMonth(expense, month))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateSpent(List<Expense> expenses, AccountingMonth month, LocalDate requestDate) {
        return expenses.stream()
                .filter(expense -> expense.inMonth(month.getYearMonth()))
                .filter(expense -> !expense.getSpentAt().isAfter(requestDate))
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
