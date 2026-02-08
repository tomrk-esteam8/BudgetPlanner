package com.budget.application.config;

import com.budget.application.dto.CreateCyclicExpenseRequest;
import com.budget.application.service.CyclicExpenseService;
import com.budget.domain.Expense;
import com.budget.domain.MonthlyFunds;
import com.budget.domain.MonthlySavings;
import com.budget.infrastructure.repository.CyclicExpenseRepository;
import com.budget.infrastructure.repository.ExpenseRepository;
import com.budget.infrastructure.repository.MonthlyFundsRepository;
import com.budget.infrastructure.repository.MonthlySavingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SeedDataRunner implements ApplicationRunner {

    @Value("${budget.seed-current-year:false}")
    private boolean seedCurrentYear;

    private final MonthlyFundsRepository monthlyFundsRepository;
    private final MonthlySavingsRepository monthlySavingsRepository;
    private final ExpenseRepository expenseRepository;
    private final CyclicExpenseRepository cyclicExpenseRepository;
    private final CyclicExpenseService cyclicExpenseService;

    @Override
    public void run(ApplicationArguments args) {
        if (!seedCurrentYear) {
            return;
        }

        int year = Year.now().getValue();
        if (!monthlyFundsRepository.findByYear(year).isEmpty()) {
            return;
        }

        seedMonthlyFunds(year);
        seedSavings();
        seedCyclicExpenses(year);
        seedExpenses(year);
    }

    private void seedMonthlyFunds(int year) {
        List<MonthlyFunds> funds = new ArrayList<>();
        for (int month = 1; month <= 12; month += 1) {
            funds.add(MonthlyFunds.builder()
                    .year(year)
                    .month(month)
                    .amount(new BigDecimal("5000.00"))
                    .build());
        }
        monthlyFundsRepository.saveAll(funds);
    }

    private void seedSavings() {
        if (!monthlySavingsRepository.findAll().isEmpty()) {
            return;
        }

        monthlySavingsRepository.save(MonthlySavings.builder()
                .amount(new BigDecimal("1000.00"))
                .build());
    }

    private void seedCyclicExpenses(int year) {
        if (!cyclicExpenseRepository.findAll().isEmpty()) {
            return;
        }

        cyclicExpenseService.createWithInitialRate(CreateCyclicExpenseRequest.builder()
                .name("Rent")
                .cycleInterval(1)
                .totalCycles(12)
                .active(true)
                .initialAmount(new BigDecimal("1500.00"))
                .validFrom(LocalDate.of(year, Month.JANUARY, 1))
                .build());

        cyclicExpenseService.createWithInitialRate(CreateCyclicExpenseRequest.builder()
                .name("Internet")
                .cycleInterval(1)
                .active(true)
                .initialAmount(new BigDecimal("80.00"))
                .validFrom(LocalDate.of(year, Month.JANUARY, 1))
                .build());

        cyclicExpenseService.createWithInitialRate(CreateCyclicExpenseRequest.builder()
                .name("Insurance")
                .cycleInterval(3)
                .active(true)
                .initialAmount(new BigDecimal("200.00"))
                .validFrom(LocalDate.of(year, Month.JANUARY, 1))
                .build());
    }

    private void seedExpenses(int year) {
        LocalDate today = LocalDate.now();
        if (!expenseRepository.findBySpentAtBetween(
                LocalDate.of(year, Month.JANUARY, 1),
                LocalDate.of(year, Month.DECEMBER, 31))
                .isEmpty()) {
            return;
        }

        List<Expense> expenses = List.of(
                Expense.builder()
                        .amount(new BigDecimal("45.50"))
                        .category("Groceries")
                        .spentAt(today.minusDays(3))
                        .build(),
                Expense.builder()
                        .amount(new BigDecimal("22.00"))
                        .category("Transport")
                        .spentAt(today.minusDays(2))
                        .build(),
                Expense.builder()
                        .amount(new BigDecimal("68.90"))
                        .category("Dining")
                        .spentAt(today.minusDays(1))
                        .build(),
                Expense.builder()
                        .amount(new BigDecimal("120.00"))
                        .category("Utilities")
                        .spentAt(LocalDate.of(year, today.getMonth(), 5))
                        .build(),
                Expense.builder()
                        .amount(new BigDecimal("35.00"))
                        .category("Health")
                        .spentAt(LocalDate.of(year, today.getMonth().minus(1), 12))
                        .build()
        );

        expenseRepository.saveAll(expenses);
    }
}
