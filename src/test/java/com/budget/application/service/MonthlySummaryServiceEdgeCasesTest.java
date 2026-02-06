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
import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonthlySummaryServiceEdgeCasesTest {

    @InjectMocks
    private MonthlySummaryServiceImpl summaryService;

    @Mock
    private CyclicExpenseCalculator cyclicExpenseCalculator;

    @Mock
    private DailyLimitCalculator dailyLimitCalculator;

    private AccountingMonth month;

    @BeforeEach
    void setUp() {
        YearMonth yearMonth = YearMonth.of(2026, 2);
        month = new AccountingMonth(yearMonth);
        // Default lenient stubbing for daily limit calculator
        lenient().when(dailyLimitCalculator.calculateFromDate(any(), any()))
                .thenReturn(BigDecimal.ZERO);
    }

    @Test
    void testCalculateSummary_NullMonthlyFunds_DefaultsToZero() {
        MonthlySummary summary = summaryService.calculate(
                month, null, null, new java.util.ArrayList<>(), new java.util.ArrayList<>());

        assertEquals(BigDecimal.ZERO, summary.getFunds());
        assertEquals(BigDecimal.ZERO, summary.getSavings());
    }

    @Test
    void testCalculateSummary_NegativeAvailable_DailyLimitIsZero() {
        MonthlyFunds funds = MonthlyFunds.builder()
                .year(2026)
                .month(2)
                .amount(new BigDecimal("1000.00"))
                .build();
        
        MonthlySavings savings = MonthlySavings.builder()
                .amount(new BigDecimal("2000.00"))
                .build();

        MonthlySummary summary = summaryService.calculate(
                month, funds, savings, new java.util.ArrayList<>(), new java.util.ArrayList<>());

        assertEquals(0, summary.getAvailable().compareTo(new BigDecimal("-1000.00")));
        assertEquals(0, summary.getDailyLimit().compareTo(BigDecimal.ZERO));
    }

    @Test
    void testCalculateSummary_ZeroAvailable_DailyLimitIsZero() {
        MonthlyFunds funds = MonthlyFunds.builder()
                .year(2026)
                .month(2)
                .amount(new BigDecimal("1000.00"))
                .build();
        
        MonthlySavings savings = MonthlySavings.builder()
                .amount(new BigDecimal("1000.00"))
                .build();

        MonthlySummary summary = summaryService.calculate(
                month, funds, savings, new java.util.ArrayList<>(), new java.util.ArrayList<>());

        assertEquals(0, summary.getAvailable().compareTo(BigDecimal.ZERO));
        assertEquals(0, summary.getDailyLimit().compareTo(BigDecimal.ZERO));
    }

    @Test
    void testCalculateSummary_NoRateForCyclicExpense_TreatsAsZero() {
        CyclicExpense expense = CyclicExpense.builder()
                .id(UUID.randomUUID())
                .name("Insurance")
                .cycleInterval(1)
                .active(true)
                .build();

        // When no rate is found, calculator returns ZERO
        when(cyclicExpenseCalculator.applies(expense, month)).thenReturn(false);

        MonthlySummary summary = summaryService.calculate(
                month,
                MonthlyFunds.builder().year(2026).month(2).amount(new BigDecimal("5000.00")).build(),
                null,
                java.util.List.of(expense),
                new java.util.ArrayList<>());

        assertEquals(BigDecimal.ZERO, summary.getFixedCosts());
    }

    @Test
    void testCalculateSummary_AllNullParameters_ResultsInZeros() {
        MonthlySummary summary = summaryService.calculate(
                month, null, null, new java.util.ArrayList<>(), new java.util.ArrayList<>());

        assertEquals(BigDecimal.ZERO, summary.getFunds());
        assertEquals(BigDecimal.ZERO, summary.getSavings());
        assertEquals(BigDecimal.ZERO, summary.getFixedCosts());
        assertEquals(BigDecimal.ZERO, summary.getSpent());
        assertEquals(BigDecimal.ZERO, summary.getAvailable());
    }

    @Test
    void testCalculateSummary_HighFixedCosts_ReducesAvailable() {
        MonthlyFunds funds = MonthlyFunds.builder()
                .year(2026)
                .month(2)
                .amount(new BigDecimal("2000.00"))
                .build();

        CyclicExpense rent = CyclicExpense.builder()
                .id(UUID.randomUUID())
                .name("Rent")
                .cycleInterval(1)
                .active(true)
                .build();

        when(cyclicExpenseCalculator.applies(rent, month)).thenReturn(true);
        when(cyclicExpenseCalculator.amountForMonth(rent, month))
                .thenReturn(new BigDecimal("1800.00"));

        MonthlySummary summary = summaryService.calculate(
                month, funds, null, java.util.List.of(rent), new java.util.ArrayList<>());

        assertEquals(new BigDecimal("200.00"), summary.getAvailable());
    }

    @Test
    void testCalculateSummary_DateInMiddleOfMonth() {
        MonthlyFunds funds = MonthlyFunds.builder()
                .year(2026)
                .month(3)
                .amount(new BigDecimal("6000.00"))
                .build();

        LocalDate midMonth = LocalDate.of(2026, 3, 15);
        when(dailyLimitCalculator.calculateFromDate(new BigDecimal("6000.00"), midMonth))
                .thenReturn(new BigDecimal("400.00"));

        MonthlySummary summary = summaryService.calculate(
                new AccountingMonth(YearMonth.of(2026, 3)),
                funds, null, new java.util.ArrayList<>(), new java.util.ArrayList<>(),
                midMonth);

        assertEquals(midMonth, summary.getDate());
        assertEquals(new BigDecimal("400.00"), summary.getDailyLimit());
    }

    @Test
    void testCalculateSummary_DateAtEndOfMonth() {
        MonthlyFunds funds = MonthlyFunds.builder()
                .year(2026)
                .month(2)
                .amount(new BigDecimal("2800.00"))
                .build();

        LocalDate lastDay = LocalDate.of(2026, 2, 28);
        when(dailyLimitCalculator.calculateFromDate(new BigDecimal("2800.00"), lastDay))
                .thenReturn(new BigDecimal("2800.00")); // Last day, all available

        MonthlySummary summary = summaryService.calculate(
                month, funds, null, new java.util.ArrayList<>(), new java.util.ArrayList<>(),
                lastDay);

        assertEquals(lastDay, summary.getDate());
        assertEquals(new BigDecimal("2800.00"), summary.getDailyLimit());
    }
}
