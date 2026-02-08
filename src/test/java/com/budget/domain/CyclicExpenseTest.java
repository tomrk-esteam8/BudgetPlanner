package com.budget.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CyclicExpenseTest {

    private CyclicExpense cyclicExpense;

    @BeforeEach
    void setUp() {
        cyclicExpense = CyclicExpense.builder()
                .id(UUID.randomUUID())
                .name("Monthly Rent")
                .cycleInterval(1)
                .totalCycles(12)
                .active(true)
                .rates(new HashSet<>())
                .build();
    }

    @Test
    void testIsActive() {
        assertTrue(cyclicExpense.isActive());

        CyclicExpense inactiveExpense = CyclicExpense.builder()
                .id(UUID.randomUUID())
                .name("Old Expense")
                .active(false)
                .build();

        assertFalse(inactiveExpense.isActive());
    }

    @Test
    void testRateForMonth() {
        CyclicExpenseRate rate1 = CyclicExpenseRate.builder()
                .amount(new BigDecimal("1000.00"))
                .validFrom(LocalDate.of(2026, 1, 1))
                .build();

        CyclicExpenseRate rate2 = CyclicExpenseRate.builder()
                .amount(new BigDecimal("1100.00"))
                .validFrom(LocalDate.of(2026, 3, 1))
                .build();

        cyclicExpense.getRates().add(rate1);
        cyclicExpense.getRates().add(rate2);

        // February should return rate1
        Optional<CyclicExpenseRate> februaryRate = cyclicExpense.rateFor(YearMonth.of(2026, 2));
        assertTrue(februaryRate.isPresent());
        assertEquals(new BigDecimal("1000.00"), februaryRate.get().getAmount());

        // March and later should return rate2
        Optional<CyclicExpenseRate> marchRate = cyclicExpense.rateFor(YearMonth.of(2026, 3));
        assertTrue(marchRate.isPresent());
        assertEquals(new BigDecimal("1100.00"), marchRate.get().getAmount());
    }

    @Test
    void testRateForMonthNoRates() {
        Optional<CyclicExpenseRate> rate = cyclicExpense.rateFor(YearMonth.of(2026, 2));
        assertFalse(rate.isPresent());
    }

    @Test
    void testRateForMonthBeforeValidFrom() {
        CyclicExpenseRate rate = CyclicExpenseRate.builder()
                .amount(new BigDecimal("1000.00"))
                .validFrom(LocalDate.of(2026, 3, 1))
                .build();

        cyclicExpense.getRates().add(rate);

        Optional<CyclicExpenseRate> februaryRate = cyclicExpense.rateFor(YearMonth.of(2026, 2));
        assertFalse(februaryRate.isPresent());
    }

    @Test
    void testRateForMonthWithMidMonthValidFrom() {
        CyclicExpenseRate rate = CyclicExpenseRate.builder()
                .amount(new BigDecimal("900.00"))
                .validFrom(LocalDate.of(2026, 2, 8))
                .build();

        cyclicExpense.getRates().add(rate);

        Optional<CyclicExpenseRate> februaryRate = cyclicExpense.rateFor(YearMonth.of(2026, 2));
        assertTrue(februaryRate.isPresent());
        assertEquals(new BigDecimal("900.00"), februaryRate.get().getAmount());
    }

    @Test
    void testRateForMonthReturnsOnlyActiveRates() {
        CyclicExpenseRate inactiveRate = CyclicExpenseRate.builder()
                .amount(new BigDecimal("1000.00"))
                .validFrom(LocalDate.of(2026, 1, 1))
                .active(false)
                .build();

        CyclicExpenseRate activeRate = CyclicExpenseRate.builder()
                .amount(new BigDecimal("1100.00"))
                .validFrom(LocalDate.of(2026, 1, 1))
                .active(true)
                .build();

        cyclicExpense.getRates().add(inactiveRate);
        cyclicExpense.getRates().add(activeRate);

        Optional<CyclicExpenseRate> rate = cyclicExpense.rateFor(YearMonth.of(2026, 2));
        assertTrue(rate.isPresent());
        assertEquals(new BigDecimal("1100.00"), rate.get().getAmount());
        assertTrue(rate.get().isActive());
    }

    @Test
    void testPrePersistDoesNotOverwriteExistingUUID() {
        UUID existingId = UUID.randomUUID();
        CyclicExpense expense = CyclicExpense.builder().id(existingId).build();

        expense.prePersist();

        assertEquals(existingId, expense.getId());
    }
}
