package com.budget.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ValidationTest {

    @Autowired
    private Validator validator;

    @Test
    void testExpenseValidation_NullAmount() {
        Expense expense = Expense.builder()
                .amount(null)
                .category("Food")
                .spentAt(LocalDate.now())
                .build();

        Set<ConstraintViolation<Expense>> violations = validator.validate(expense);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Amount cannot be null")));
    }

    @Test
    void testExpenseValidation_NegativeAmount() {
        Expense expense = Expense.builder()
                .amount(new BigDecimal("-50.00"))
                .category("Food")
                .spentAt(LocalDate.now())
                .build();

        Set<ConstraintViolation<Expense>> violations = validator.validate(expense);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Amount must be greater than 0")));
    }

    @Test
    void testExpenseValidation_ZeroAmount() {
        Expense expense = Expense.builder()
                .amount(BigDecimal.ZERO)
                .category("Food")
                .spentAt(LocalDate.now())
                .build();

        Set<ConstraintViolation<Expense>> violations = validator.validate(expense);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Amount must be greater than 0")));
    }

    @Test
    void testExpenseValidation_BlankCategory() {
        Expense expense = Expense.builder()
                .amount(new BigDecimal("50.00"))
                .category("   ")
                .spentAt(LocalDate.now())
                .build();

        Set<ConstraintViolation<Expense>> violations = validator.validate(expense);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Category cannot be blank")));
    }

    @Test
    void testExpenseValidation_FutureDate() {
        Expense expense = Expense.builder()
                .amount(new BigDecimal("50.00"))
                .category("Food")
                .spentAt(LocalDate.now().plusDays(1))
                .build();

        Set<ConstraintViolation<Expense>> violations = validator.validate(expense);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("cannot be in the future")));
    }

    @Test
    void testExpenseValidation_ValidExpense() {
        Expense expense = Expense.builder()
                .amount(new BigDecimal("50.00"))
                .category("Food")
                .spentAt(LocalDate.now())
                .build();

        Set<ConstraintViolation<Expense>> violations = validator.validate(expense);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testMonthlyFundsValidation_InvalidYear_TooLow() {
        MonthlyFunds funds = MonthlyFunds.builder()
                .year(1899)
                .month(5)
                .amount(new BigDecimal("5000.00"))
                .build();

        Set<ConstraintViolation<MonthlyFunds>> violations = validator.validate(funds);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Year must be 1900 or later")));
    }

    @Test
    void testMonthlyFundsValidation_InvalidYear_TooHigh() {
        MonthlyFunds funds = MonthlyFunds.builder()
                .year(2101)
                .month(5)
                .amount(new BigDecimal("5000.00"))
                .build();

        Set<ConstraintViolation<MonthlyFunds>> violations = validator.validate(funds);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Year must be 2100 or earlier")));
    }

    @Test
    void testMonthlyFundsValidation_InvalidMonth_TooLow() {
        MonthlyFunds funds = MonthlyFunds.builder()
                .year(2026)
                .month(0)
                .amount(new BigDecimal("5000.00"))
                .build();

        Set<ConstraintViolation<MonthlyFunds>> violations = validator.validate(funds);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Month must be between 1 and 12")));
    }

    @Test
    void testMonthlyFundsValidation_InvalidMonth_TooHigh() {
        MonthlyFunds funds = MonthlyFunds.builder()
                .year(2026)
                .month(13)
                .amount(new BigDecimal("5000.00"))
                .build();

        Set<ConstraintViolation<MonthlyFunds>> violations = validator.validate(funds);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Month must be between 1 and 12")));
    }

    @Test
    void testMonthlyFundsValidation_InvalidAmount() {
        MonthlyFunds funds = MonthlyFunds.builder()
                .year(2026)
                .month(5)
                .amount(new BigDecimal("0.00"))
                .build();

        Set<ConstraintViolation<MonthlyFunds>> violations = validator.validate(funds);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Amount must be greater than 0")));
    }

    @Test
    void testMonthlyFundsValidation_ValidFunds() {
        MonthlyFunds funds = MonthlyFunds.builder()
                .year(2026)
                .month(5)
                .amount(new BigDecimal("5000.00"))
                .build();

        Set<ConstraintViolation<MonthlyFunds>> violations = validator.validate(funds);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testCyclicExpenseValidation_InvalidCycleInterval() {
        CyclicExpense expense = CyclicExpense.builder()
                .id(UUID.randomUUID())
                .name("Rent")
                .cycleInterval(0)
                .active(true)
                .build();

        Set<ConstraintViolation<CyclicExpense>> violations = validator.validate(expense);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Cycle interval must be at least 1")));
    }

    @Test
    void testCyclicExpenseValidation_NegativeCycleInterval() {
        CyclicExpense expense = CyclicExpense.builder()
                .id(UUID.randomUUID())
                .name("Rent")
                .cycleInterval(-1)
                .active(true)
                .build();

        Set<ConstraintViolation<CyclicExpense>> violations = validator.validate(expense);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Cycle interval must be at least 1")));
    }

    @Test
    void testCyclicExpenseValidation_BlankName() {
        CyclicExpense expense = CyclicExpense.builder()
                .id(UUID.randomUUID())
                .name("   ")
                .cycleInterval(1)
                .active(true)
                .build();

        Set<ConstraintViolation<CyclicExpense>> violations = validator.validate(expense);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("name cannot be blank")));
    }

    @Test
    void testCyclicExpenseValidation_ValidExpense() {
        CyclicExpense expense = CyclicExpense.builder()
                .id(UUID.randomUUID())
                .name("Rent")
                .cycleInterval(1)
                .active(true)
                .build();

        Set<ConstraintViolation<CyclicExpense>> violations = validator.validate(expense);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testCyclicExpenseRateValidation_InvalidAmount() {
        CyclicExpenseRate rate = CyclicExpenseRate.builder()
                .amount(new BigDecimal("-500.00"))
                .validFrom(LocalDate.now())
                .active(true)
                .build();

        Set<ConstraintViolation<CyclicExpenseRate>> violations = validator.validate(rate);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Amount must be greater than 0")));
    }

    @Test
    void testCyclicExpenseRateValidation_FutureDate() {
        CyclicExpenseRate rate = CyclicExpenseRate.builder()
                .amount(new BigDecimal("500.00"))
                .validFrom(LocalDate.now().plusDays(1))
                .active(true)
                .build();

        Set<ConstraintViolation<CyclicExpenseRate>> violations = validator.validate(rate);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("cannot be in the future")));
    }

    @Test
    void testCyclicExpenseRateValidation_ValidRate() {
        CyclicExpenseRate rate = CyclicExpenseRate.builder()
                .amount(new BigDecimal("500.00"))
                .validFrom(LocalDate.now())
                .active(true)
                .build();

        Set<ConstraintViolation<CyclicExpenseRate>> violations = validator.validate(rate);
        assertTrue(violations.isEmpty());
    }
}
