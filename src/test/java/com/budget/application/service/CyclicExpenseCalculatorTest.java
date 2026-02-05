package com.budget.application.service;

import com.budget.domain.AccountingMonth;
import com.budget.domain.CyclicExpense;
import com.budget.domain.CyclicExpenseRate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CyclicExpenseCalculatorTest {

    @InjectMocks
    private CyclicExpenseCalculator calculator;

    private CyclicExpense monthlyExpense;
    private CyclicExpense trimontlyExpense;
    private AccountingMonth month;

    @BeforeEach
    void setUp() {
        month = new AccountingMonth(YearMonth.of(2026, 2));

        // Monthly expense (cycleInterval=1)
        CyclicExpenseRate monthlyRate = CyclicExpenseRate.builder()
                .id(1L)
                .amount(new BigDecimal("1500.00"))
                .validFrom(LocalDate.of(2026, 1, 1))
                .build();

        Set<CyclicExpenseRate> monthlyRates = new HashSet<>();
        monthlyRates.add(monthlyRate);

        monthlyExpense = CyclicExpense.builder()
                .id(UUID.randomUUID())
                .name("Monthly Rent")
                .cycleInterval(1)
                .totalCycles(12)
                .active(true)
                .rates(monthlyRates)
                .build();

        // Trimonthly expense (cycleInterval=3)
        CyclicExpenseRate trimontlyRate = CyclicExpenseRate.builder()
                .id(2L)
                .amount(new BigDecimal("3000.00"))
                .validFrom(LocalDate.of(2026, 1, 1))
                .build();

        Set<CyclicExpenseRate> trimontlyRates = new HashSet<>();
        trimontlyRates.add(trimontlyRate);

        trimontlyExpense = CyclicExpense.builder()
                .id(UUID.randomUUID())
                .name("Trimonthly Expense")
                .cycleInterval(3)
                .totalCycles(4)
                .active(true)
                .rates(trimontlyRates)
                .build();
    }

    @Test
    void testMonthlyExpenseAppliesToEveryMonth() {
        assertTrue(calculator.applies(monthlyExpense, month));
        
        // Test next month
        AccountingMonth nextMonth = new AccountingMonth(YearMonth.of(2026, 3));
        assertTrue(calculator.applies(monthlyExpense, nextMonth));
    }

    @Test
    void testTrimontlyExpenseAppliesToSpecificMonths() {
        // February (1 month after January) - should NOT apply
        assertFalse(calculator.applies(trimontlyExpense, month));
        
        // April (3 months after January) - should apply
        AccountingMonth april = new AccountingMonth(YearMonth.of(2026, 4));
        assertTrue(calculator.applies(trimontlyExpense, april));
        
        // July (6 months after January) - should apply
        AccountingMonth july = new AccountingMonth(YearMonth.of(2026, 7));
        assertTrue(calculator.applies(trimontlyExpense, july));
    }

    @Test
    void testDoesNotApplyToInactiveExpense() {
        monthlyExpense.setActive(false);
        assertFalse(calculator.applies(monthlyExpense, month));
    }

    @Test
    void testAmountForMonthWithRate() {
        BigDecimal amount = calculator.amountForMonth(monthlyExpense, month);
        assertEquals(new BigDecimal("1500.00"), amount);
    }

    @Test
    void testAmountForMonthWithoutRate() {
        monthlyExpense.setRates(new HashSet<>());
        BigDecimal amount = calculator.amountForMonth(monthlyExpense, month);
        assertEquals(BigDecimal.ZERO, amount);
    }
}
