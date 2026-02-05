package com.budget.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseTest {

    private Expense expense;

    @BeforeEach
    void setUp() {
        expense = Expense.builder()
                .id(1L)
                .amount(new BigDecimal("50.00"))
                .category("Food")
                .spentAt(LocalDate.of(2026, 2, 5))
                .build();
    }

    @Test
    void testAmount() {
        assertEquals(new BigDecimal("50.00"), expense.amount());
    }

    @Test
    void testInMonthSameMonth() {
        YearMonth month = YearMonth.of(2026, 2);
        assertTrue(expense.inMonth(month));
    }

    @Test
    void testInMonthDifferentMonth() {
        YearMonth month = YearMonth.of(2026, 3);
        assertFalse(expense.inMonth(month));
    }

    @Test
    void testInMonthDifferentYear() {
        YearMonth month = YearMonth.of(2025, 2);
        assertFalse(expense.inMonth(month));
    }
}
