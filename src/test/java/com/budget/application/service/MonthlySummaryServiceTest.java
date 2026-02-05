package com.budget.application.service;

import com.budget.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MonthlySummaryServiceTest {

    @InjectMocks
    private MonthlySummaryServiceImpl summaryService;

    @Mock
    private CyclicExpenseCalculator cyclicExpenseCalculator;

    @Mock
    private DailyLimitCalculator dailyLimitCalculator;

    private AccountingMonth month;
    private MonthlyFunds funds;
    private MonthlySavings savings;
    private List<CyclicExpense> cyclicExpenses;
    private List<Expense> expenses;

    @BeforeEach
    void setUp() {
        month = new AccountingMonth(YearMonth.of(2026, 2));
        
        funds = MonthlyFunds.builder()
                .year(2026)
                .month(2)
                .amount(new BigDecimal("5000.00"))
                .build();
        
        savings = MonthlySavings.builder()
                .amount(new BigDecimal("1000.00"))
                .build();
        
        cyclicExpenses = new ArrayList<>();
        expenses = new ArrayList<>();
    }

    @Test
    void testCalculateSummaryWithData() {
        Expense expense = Expense.builder()
                .amount(new BigDecimal("100.00"))
                .category("Food")
                .spentAt(LocalDate.of(2026, 2, 5))
                .build();
        expenses.add(expense);

        CyclicExpense cyclicExpense = CyclicExpense.builder()
                .id(UUID.randomUUID())
                .name("Rent")
                .cycleInterval(1)
                .totalCycles(12)
                .active(true)
                .rates(new HashSet<>())
                .build();
        cyclicExpenses.add(cyclicExpense);

        org.mockito.Mockito.when(cyclicExpenseCalculator.applies(cyclicExpense, month))
                .thenReturn(true);
        org.mockito.Mockito.when(cyclicExpenseCalculator.amountForMonth(cyclicExpense, month))
                .thenReturn(new BigDecimal("1500.00"));
        // available = funds (5000) - savings (1000) - fixedCosts (1500) - spent (100) = 2400
        // For February 5, 2026: 28 - 5 + 1 = 24 remaining days
        org.mockito.Mockito.when(dailyLimitCalculator.calculateFromDate(
                new BigDecimal("2400.00"), LocalDate.of(2026, 2, 5)))
                .thenReturn(new BigDecimal("100.00"));

        MonthlySummary summary = summaryService.calculate(month, funds, savings, cyclicExpenses, expenses, LocalDate.of(2026, 2, 5));

        assertEquals(LocalDate.of(2026, 2, 5), summary.getDate());
        assertEquals(new BigDecimal("5000.00"), summary.getFunds());
        assertEquals(new BigDecimal("1000.00"), summary.getSavings());
        assertEquals(new BigDecimal("1500.00"), summary.getFixedCosts());
        assertEquals(new BigDecimal("100.00"), summary.getSpent());
        assertEquals(new BigDecimal("2400.00"), summary.getAvailable());
    }

    @Test
    void testCalculateSummaryWithoutData() {
        MonthlySummary summary = summaryService.calculate(month, null, null, cyclicExpenses, expenses);

        assertEquals(LocalDate.of(2026, 2, 6), summary.getDate());
        assertEquals(BigDecimal.ZERO, summary.getFunds());
        assertEquals(BigDecimal.ZERO, summary.getSavings());
    }
}
