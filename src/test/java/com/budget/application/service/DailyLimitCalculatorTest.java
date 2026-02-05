package com.budget.application.service;

import com.budget.domain.AccountingMonth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DailyLimitCalculatorTest {

    @InjectMocks
    private DailyLimitCalculator calculator;

    private AccountingMonth month;

    @BeforeEach
    void setUp() {
        month = new AccountingMonth(YearMonth.of(2026, 2)); // 28 days
    }

    @Test
    void testCalculateDailyLimit() {
        BigDecimal available = new BigDecimal("280.00");
        BigDecimal dailyLimit = calculator.calculate(available, month);
        
        assertEquals(new BigDecimal("10.00"), dailyLimit);
    }

    @Test
    void testCalculateDailyLimitZeroAvailable() {
        BigDecimal available = BigDecimal.ZERO;
        BigDecimal dailyLimit = calculator.calculate(available, month);
        
        assertEquals(BigDecimal.ZERO, dailyLimit);
    }

    @Test
    void testCalculateDailyLimitNegativeAvailable() {
        BigDecimal available = new BigDecimal("-100.00");
        BigDecimal dailyLimit = calculator.calculate(available, month);
        
        assertEquals(BigDecimal.ZERO, dailyLimit);
    }

    @Test
    void testCalculateDailyLimitRounding() {
        BigDecimal available = new BigDecimal("100.00");
        BigDecimal dailyLimit = calculator.calculate(available, month);
        
        assertEquals(new BigDecimal("3.57"), dailyLimit);
    }

    @Test
    void testCalculateFromDateMidMonth() {
        // March 17 in 31-day month: 31 - 17 + 1 = 15 remaining days
        LocalDate date = LocalDate.of(2026, 3, 17);
        BigDecimal available = new BigDecimal("800.00");
        BigDecimal dailyLimit = calculator.calculateFromDate(available, date);
        
        // 800 / 15 = 53.333... → 53.33 (HALF_DOWN)
        assertEquals(new BigDecimal("53.33"), dailyLimit);
    }

    @Test
    void testCalculateFromDateLastDay() {
        // March 31 in 31-day month: 31 - 31 + 1 = 1 remaining day
        LocalDate date = LocalDate.of(2026, 3, 31);
        BigDecimal available = new BigDecimal("800.00");
        BigDecimal dailyLimit = calculator.calculateFromDate(available, date);
        
        // 800 / 1 = 800.00
        assertEquals(new BigDecimal("800.00"), dailyLimit);
    }

    @Test
    void testCalculateFromDateFirstDay() {
        // March 1 in 31-day month: 31 - 1 + 1 = 31 remaining days
        LocalDate date = LocalDate.of(2026, 3, 1);
        BigDecimal available = new BigDecimal("800.00");
        BigDecimal dailyLimit = calculator.calculateFromDate(available, date);
        
        // 800 / 31 = 25.8064... → 25.81 (HALF_DOWN rounds the 6 up)
        assertEquals(new BigDecimal("25.81"), dailyLimit);
    }

    @Test
    void testCalculateFromDateNegativeAvailable() {
        LocalDate date = LocalDate.of(2026, 3, 17);
        BigDecimal available = new BigDecimal("-100.00");
        BigDecimal dailyLimit = calculator.calculateFromDate(available, date);
        
        assertEquals(BigDecimal.ZERO, dailyLimit);
    }

    @Test
    void testCalculateFromDateZeroAvailable() {
        LocalDate date = LocalDate.of(2026, 3, 17);
        BigDecimal available = BigDecimal.ZERO;
        BigDecimal dailyLimit = calculator.calculateFromDate(available, date);
        
        assertEquals(BigDecimal.ZERO, dailyLimit);
    }

    @Test
    void testCalculateFromDateFebruary() {
        // February 14 in 28-day month: 28 - 14 + 1 = 15 remaining days
        LocalDate date = LocalDate.of(2026, 2, 14);
        BigDecimal available = new BigDecimal("750.00");
        BigDecimal dailyLimit = calculator.calculateFromDate(available, date);
        
        // 750 / 15 = 50.00
        assertEquals(new BigDecimal("50.00"), dailyLimit);
    }
}
