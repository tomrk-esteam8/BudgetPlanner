package com.budget.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;

class AccountingMonthTest {

    private AccountingMonth accountingMonth;

    @BeforeEach
    void setUp() {
        accountingMonth = new AccountingMonth(YearMonth.of(2026, 2));
    }

    @Test
    void testDaysInMonth() {
        assertEquals(28, accountingMonth.daysInMonth());
    }

    @Test
    void testFirstDay() {
        assertEquals(LocalDate.of(2026, 2, 1), accountingMonth.firstDay());
    }

    @Test
    void testLastDay() {
        assertEquals(LocalDate.of(2026, 2, 28), accountingMonth.lastDay());
    }

    @Test
    void testLeapYearFebruary() {
        AccountingMonth leapYear = new AccountingMonth(YearMonth.of(2024, 2));
        assertEquals(29, leapYear.daysInMonth());
        assertEquals(LocalDate.of(2024, 2, 29), leapYear.lastDay());
    }

    @Test
    void testJanuaryHas31Days() {
        AccountingMonth january = new AccountingMonth(YearMonth.of(2026, 1));
        assertEquals(31, january.daysInMonth());
    }
}
