package com.budget.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MonthlyFundsTest {

    @Test
    void testPeriodMethod() {
        MonthlyFunds funds = MonthlyFunds.builder()
                .year(2026)
                .month(2)
                .amount(new BigDecimal("5000.00"))
                .build();

        YearMonth period = funds.period();

        assertEquals(YearMonth.of(2026, 2), period);
    }

    @Test
    void testAmountMethod() {
        BigDecimal amount = new BigDecimal("5000.00");
        MonthlyFunds funds = MonthlyFunds.builder()
                .year(2026)
                .month(2)
                .amount(amount)
                .build();

        BigDecimal result = funds.amount();

        assertEquals(amount, result);
    }

    @Test
    void testMonthlyFundsBuilder() {
        MonthlyFunds funds = MonthlyFunds.builder()
                .id(1L)
                .year(2026)
                .month(2)
                .amount(new BigDecimal("5000.00"))
                .build();

        assertEquals(1L, funds.getId());
        assertEquals(2026, funds.getYear());
        assertEquals(2, funds.getMonth());
        assertEquals(new BigDecimal("5000.00"), funds.getAmount());
    }
}
