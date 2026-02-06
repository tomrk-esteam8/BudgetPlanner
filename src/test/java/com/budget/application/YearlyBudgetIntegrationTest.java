package com.budget.application;

import com.budget.application.dto.CreateCyclicExpenseRequest;
import com.budget.application.service.*;
import com.budget.domain.*;
import com.budget.infrastructure.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class YearlyBudgetIntegrationTest {

    @Autowired
    private MonthlyFundsService monthlyFundsService;

    @Autowired
    private CyclicExpenseService cyclicExpenseService;

    @Autowired
    private MonthlyFundsRepository monthlyFundsRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private CyclicExpenseRepository cyclicExpenseRepository;

    @Autowired
    private MonthlySavingsRepository monthlySavingsRepository;

    @Autowired
    private MonthlySummaryService monthlySummaryService;

    @BeforeEach
    void setUp() {
        // Clean up repositories
        expenseRepository.deleteAll();
        cyclicExpenseRepository.deleteAll();
        monthlyFundsRepository.deleteAll();
    }

    @Test
    void testCompleteYearlyBudgetWithCyclicExpensesAndRandomExpenses() {
        // Setup
        setupYearlyBudget();

        // Verify monthly summaries for first 6 months (before insurance rate change)
        verifyJanuarySummary(); // rent 1500 + car 350 + insurance 200
        verifyFebruarySummary(); // rent 1500 + car 350
        verifyMarchSummary(); // rent 1500 + car 350
        verifyAprilSummary(); // rent 1500 + car 350 + insurance 200
        verifyMaySummary(); // rent 1500 + car 350
        verifyJuneSummary(); // rent 1500 + car 350

        // Change insurance cost in July (month 7)
        // Need to refresh from DB to avoid lazy loading issues
        CyclicExpense insuranceFromDb = cyclicExpenseRepository.findAll().stream()
                .filter(e -> "Car Insurance".equals(e.getName()))
                .findFirst()
                .orElseThrow();
        CyclicExpenseRate newInsuranceRate = CyclicExpenseRate.builder()
                .amount(new BigDecimal("250.00"))
                .validFrom(LocalDate.of(2025, 7, 1))
                .active(true)
                .build();
        cyclicExpenseService.addNewRate(insuranceFromDb.getId(), newInsuranceRate);

        // Verify remaining months with new insurance rate
        verifyJulySummary(); // rent 1500 + car 350 + insurance 250 (new rate)
        verifyAugustSummary(); // rent 1500 + car 350
        verifyMemberSummary(); // rent 1500 + car 350
        verifyOctoberSummary(); // rent 1500 + car 350 + insurance 250
        verifyNovemberSummary(); // rent 1500 + car 350
        verifyDecemberSummary(); // rent 1500 + car 350

        // Verify daily limit calculations
        verifyDailyLimitMidMonthAndEndOfMonth();
    }

    private void addExpense(BigDecimal amount, String category, LocalDate spentAt) {
        Expense expense = Expense.builder()
                .amount(amount)
                .category(category)
                .spentAt(spentAt)
                .build();
        expenseRepository.save(expense);
    }

    private void setupYearlyBudget() {
        // 1. Create monthly funds for each month in 2025
        for (int month = 1; month <= 12; month++) {
            MonthlyFunds funds = MonthlyFunds.builder()
                    .year(2025)
                    .month(month)
                    .amount(new BigDecimal("5000.00"))
                    .build();
            monthlyFundsService.save(funds);
        }

        // 2. Create cyclic expenses with different intervals
        // Rent: every month (cycleInterval = 1)
        cyclicExpenseService.createWithInitialRate(
                CreateCyclicExpenseRequest.builder()
                        .name("Rent")
                        .cycleInterval(1)
                        .totalCycles(12)
                        .active(true)
                        .initialAmount(new BigDecimal("1500.00"))
                        .validFrom(LocalDate.of(2025, 1, 1))
                        .build()
        );

        // Car: every month (cycleInterval = 1)
        cyclicExpenseService.createWithInitialRate(
                CreateCyclicExpenseRequest.builder()
                        .name("Car Payment")
                        .cycleInterval(1)
                        .totalCycles(12)
                        .active(true)
                        .initialAmount(new BigDecimal("350.00"))
                        .validFrom(LocalDate.of(2025, 1, 1))
                        .build()
        );

        // Insurance: every 3 months (Jan, Apr, Jul, Oct)
        cyclicExpenseService.createWithInitialRate(
                CreateCyclicExpenseRequest.builder()
                        .name("Car Insurance")
                        .cycleInterval(3)
                        .totalCycles(4)
                        .active(true)
                        .initialAmount(new BigDecimal("200.00"))
                        .validFrom(LocalDate.of(2025, 1, 1))
                        .build()
        );

        // 3. Add random expenses throughout the year
        // January expenses
        addExpense(new BigDecimal("150.00"), "Groceries", LocalDate.of(2025, 1, 5));
        addExpense(new BigDecimal("45.00"), "Gas", LocalDate.of(2025, 1, 10));
        addExpense(new BigDecimal("30.00"), "Hobby", LocalDate.of(2025, 1, 15));

        // February expenses
        addExpense(new BigDecimal("200.00"), "Groceries", LocalDate.of(2025, 2, 8));
        addExpense(new BigDecimal("50.00"), "Gas", LocalDate.of(2025, 2, 12));

        // March expenses
        addExpense(new BigDecimal("160.00"), "Groceries", LocalDate.of(2025, 3, 3));
        addExpense(new BigDecimal("55.00"), "Gas", LocalDate.of(2025, 3, 18));
        addExpense(new BigDecimal("25.00"), "Hobby", LocalDate.of(2025, 3, 20));

        // April expenses (includes insurance)
        addExpense(new BigDecimal("180.00"), "Groceries", LocalDate.of(2025, 4, 5));
        addExpense(new BigDecimal("48.00"), "Gas", LocalDate.of(2025, 4, 14));

        // May expenses
        addExpense(new BigDecimal("170.00"), "Groceries", LocalDate.of(2025, 5, 6));
        addExpense(new BigDecimal("52.00"), "Gas", LocalDate.of(2025, 5, 19));
        addExpense(new BigDecimal("35.00"), "Hobby", LocalDate.of(2025, 5, 25));

        // June expenses
        addExpense(new BigDecimal("190.00"), "Groceries", LocalDate.of(2025, 6, 2));
        addExpense(new BigDecimal("60.00"), "Gas", LocalDate.of(2025, 6, 16));

        // July expenses (insurance cost changes - will do this AFTER verifying first 6 months)
        addExpense(new BigDecimal("175.00"), "Groceries", LocalDate.of(2025, 7, 8));
        addExpense(new BigDecimal("58.00"), "Gas", LocalDate.of(2025, 7, 17));
        addExpense(new BigDecimal("40.00"), "Hobby", LocalDate.of(2025, 7, 22));

        // August expenses
        addExpense(new BigDecimal("185.00"), "Groceries", LocalDate.of(2025, 8, 7));
        addExpense(new BigDecimal("55.00"), "Gas", LocalDate.of(2025, 8, 20));

        // September expenses
        addExpense(new BigDecimal("165.00"), "Groceries", LocalDate.of(2025, 9, 4));
        addExpense(new BigDecimal("50.00"), "Gas", LocalDate.of(2025, 9, 18));
        addExpense(new BigDecimal("30.00"), "Hobby", LocalDate.of(2025, 9, 28));

        // October expenses (includes insurance)
        addExpense(new BigDecimal("195.00"), "Groceries", LocalDate.of(2025, 10, 5));
        addExpense(new BigDecimal("62.00"), "Gas", LocalDate.of(2025, 10, 19));

        // November expenses
        addExpense(new BigDecimal("180.00"), "Groceries", LocalDate.of(2025, 11, 3));
        addExpense(new BigDecimal("58.00"), "Gas", LocalDate.of(2025, 11, 17));
        addExpense(new BigDecimal("45.00"), "Hobby", LocalDate.of(2025, 11, 25));

        // December expenses
        addExpense(new BigDecimal("220.00"), "Groceries", LocalDate.of(2025, 12, 6));
        addExpense(new BigDecimal("65.00"), "Gas", LocalDate.of(2025, 12, 20));
    }

    private MonthlySummary getSummarySummary(int month) {
        YearMonth yearMonth = YearMonth.of(2025, month);
        AccountingMonth accountingMonth = new AccountingMonth(yearMonth);
        
        MonthlyFunds funds = monthlyFundsRepository.findByYearAndMonth(2025, month).orElseThrow();
        MonthlySavings savings = MonthlySavings.builder().amount(new BigDecimal("1000.00")).build();
        List<CyclicExpense> cyclicExpenses = cyclicExpenseRepository.findAll();
        List<Expense> expenses = expenseRepository.findBySpentAtBetween(
                yearMonth.atDay(1),
                yearMonth.atEndOfMonth()
        );

        // Pass end-of-month date to include all expenses for the month
        return monthlySummaryService.calculate(accountingMonth, funds, savings, cyclicExpenses, expenses, yearMonth.atEndOfMonth());
    }

    private void verifyJanuarySummary() {
        // January: rent 1500 + car 350 + insurance 200 = 2050 fixed costs
        // Expenses: 150 + 45 + 30 = 225
        // Available: 5000 - 1000 (savings) - 2050 - 225 = 1725
        MonthlySummary summary = getSummarySummary(1);

        assertEquals(new BigDecimal("5000.00"), summary.getFunds());
        assertEquals(new BigDecimal("1000.00"), summary.getSavings());
        assertEquals(new BigDecimal("2050.00"), summary.getFixedCosts()); // 1500 + 350 + 200
        assertEquals(new BigDecimal("225.00"), summary.getSpent()); // 150 + 45 + 30
        assertEquals(new BigDecimal("1725.00"), summary.getAvailable()); // 5000 - 1000 - 2050 - 225
    }

    private void verifyFebruarySummary() {
        // February: rent 1500 + car 350 = 1850 fixed costs (no insurance)
        // Expenses: 200 + 50 = 250
        // Available: 5000 - 1000 - 1850 - 250 = 1900
        MonthlySummary summary = getSummarySummary(2);

        assertEquals(new BigDecimal("5000.00"), summary.getFunds());
        assertEquals(new BigDecimal("1000.00"), summary.getSavings());
        assertEquals(new BigDecimal("1850.00"), summary.getFixedCosts()); // 1500 + 350
        assertEquals(new BigDecimal("250.00"), summary.getSpent()); // 200 + 50
        assertEquals(new BigDecimal("1900.00"), summary.getAvailable()); // 5000 - 1000 - 1850 - 250
    }

    private void verifyMarchSummary() {
        // March: rent 1500 + car 350 = 1850 fixed costs (no insurance)
        // Expenses: 160 + 55 + 25 = 240
        // Available: 5000 - 1000 - 1850 - 240 = 1910
        MonthlySummary summary = getSummarySummary(3);

        assertEquals(new BigDecimal("5000.00"), summary.getFunds());
        assertEquals(new BigDecimal("1000.00"), summary.getSavings());
        assertEquals(new BigDecimal("1850.00"), summary.getFixedCosts()); // 1500 + 350
        assertEquals(new BigDecimal("240.00"), summary.getSpent()); // 160 + 55 + 25
        assertEquals(new BigDecimal("1910.00"), summary.getAvailable()); // 5000 - 1000 - 1850 - 240
    }

    private void verifyAprilSummary() {
        // April: rent 1500 + car 350 + insurance 200 = 2050 fixed costs
        // Expenses: 180 + 48 = 228
        // Available: 5000 - 1000 - 2050 - 228 = 1722
        MonthlySummary summary = getSummarySummary(4);

        assertEquals(new BigDecimal("5000.00"), summary.getFunds());
        assertEquals(new BigDecimal("1000.00"), summary.getSavings());
        assertEquals(new BigDecimal("2050.00"), summary.getFixedCosts()); // 1500 + 350 + 200
        assertEquals(new BigDecimal("228.00"), summary.getSpent()); // 180 + 48
        assertEquals(new BigDecimal("1722.00"), summary.getAvailable()); // 5000 - 1000 - 2050 - 228
    }

    private void verifyMaySummary() {
        // May: rent 1500 + car 350 = 1850 fixed costs (no insurance)
        // Expenses: 170 + 52 + 35 = 257
        // Available: 5000 - 1000 - 1850 - 257 = 1893
        MonthlySummary summary = getSummarySummary(5);

        assertEquals(new BigDecimal("5000.00"), summary.getFunds());
        assertEquals(new BigDecimal("1000.00"), summary.getSavings());
        assertEquals(new BigDecimal("1850.00"), summary.getFixedCosts()); // 1500 + 350
        assertEquals(new BigDecimal("257.00"), summary.getSpent()); // 170 + 52 + 35
        assertEquals(new BigDecimal("1893.00"), summary.getAvailable()); // 5000 - 1000 - 1850 - 257
    }

    private void verifyJuneSummary() {
        // June: rent 1500 + car 350 = 1850 fixed costs (no insurance)
        // Expenses: 190 + 60 = 250
        // Available: 5000 - 1000 - 1850 - 250 = 1900
        MonthlySummary summary = getSummarySummary(6);

        assertEquals(new BigDecimal("5000.00"), summary.getFunds());
        assertEquals(new BigDecimal("1000.00"), summary.getSavings());
        assertEquals(new BigDecimal("1850.00"), summary.getFixedCosts()); // 1500 + 350
        assertEquals(new BigDecimal("250.00"), summary.getSpent()); // 190 + 60
        assertEquals(new BigDecimal("1900.00"), summary.getAvailable()); // 5000 - 1000 - 1850 - 250
    }

    private void verifyJulySummary() {
        // July: rent 1500 + car 350 + insurance 250 (NEW RATE) = 2100 fixed costs
        // Expenses: 175 + 58 + 40 = 273
        // Available: 5000 - 1000 - 2100 - 273 = 1627
        MonthlySummary summary = getSummarySummary(7);

        assertEquals(new BigDecimal("5000.00"), summary.getFunds());
        assertEquals(new BigDecimal("1000.00"), summary.getSavings());
        assertEquals(new BigDecimal("2100.00"), summary.getFixedCosts()); // 1500 + 350 + 250 (new insurance rate)
        assertEquals(new BigDecimal("273.00"), summary.getSpent()); // 175 + 58 + 40
        assertEquals(new BigDecimal("1627.00"), summary.getAvailable()); // 5000 - 1000 - 2100 - 273
    }

    private void verifyAugustSummary() {
        // August: rent 1500 + car 350 = 1850 fixed costs (no insurance)
        // Expenses: 185 + 55 = 240
        // Available: 5000 - 1000 - 1850 - 240 = 1910
        MonthlySummary summary = getSummarySummary(8);

        assertEquals(new BigDecimal("5000.00"), summary.getFunds());
        assertEquals(new BigDecimal("1000.00"), summary.getSavings());
        assertEquals(new BigDecimal("1850.00"), summary.getFixedCosts()); // 1500 + 350
        assertEquals(new BigDecimal("240.00"), summary.getSpent()); // 185 + 55
        assertEquals(new BigDecimal("1910.00"), summary.getAvailable()); // 5000 - 1000 - 1850 - 240
    }

    private void verifyMemberSummary() {
        // September: rent 1500 + car 350 = 1850 fixed costs (no insurance)
        // Expenses: 165 + 50 + 30 = 245
        // Available: 5000 - 1000 - 1850 - 245 = 1905
        MonthlySummary summary = getSummarySummary(9);

        assertEquals(new BigDecimal("5000.00"), summary.getFunds());
        assertEquals(new BigDecimal("1000.00"), summary.getSavings());
        assertEquals(new BigDecimal("1850.00"), summary.getFixedCosts()); // 1500 + 350
        assertEquals(new BigDecimal("245.00"), summary.getSpent()); // 165 + 50 + 30
        assertEquals(new BigDecimal("1905.00"), summary.getAvailable()); // 5000 - 1000 - 1850 - 245
    }

    private void verifyOctoberSummary() {
        // October: rent 1500 + car 350 + insurance 250 = 2100 fixed costs (new rate applies)
        // Expenses: 195 + 62 = 257
        // Available: 5000 - 1000 - 2100 - 257 = 1643
        MonthlySummary summary = getSummarySummary(10);

        assertEquals(new BigDecimal("5000.00"), summary.getFunds());
        assertEquals(new BigDecimal("1000.00"), summary.getSavings());
        assertEquals(new BigDecimal("2100.00"), summary.getFixedCosts()); // 1500 + 350 + 250 (new insurance rate)
        assertEquals(new BigDecimal("257.00"), summary.getSpent()); // 195 + 62
        assertEquals(new BigDecimal("1643.00"), summary.getAvailable()); // 5000 - 1000 - 2100 - 257
    }

    private void verifyNovemberSummary() {
        // November: rent 1500 + car 350 = 1850 fixed costs (no insurance)
        // Expenses: 180 + 58 + 45 = 283
        // Available: 5000 - 1000 - 1850 - 283 = 1867
        MonthlySummary summary = getSummarySummary(11);

        assertEquals(new BigDecimal("5000.00"), summary.getFunds());
        assertEquals(new BigDecimal("1000.00"), summary.getSavings());
        assertEquals(new BigDecimal("1850.00"), summary.getFixedCosts()); // 1500 + 350
        assertEquals(new BigDecimal("283.00"), summary.getSpent()); // 180 + 58 + 45
        assertEquals(new BigDecimal("1867.00"), summary.getAvailable()); // 5000 - 1000 - 1850 - 283
    }

    private void verifyDecemberSummary() {
        // December: rent 1500 + car 350 = 1850 fixed costs (no insurance)
        // Expenses: 220 + 65 = 285
        // Available: 5000 - 1000 - 1850 - 285 = 1865
        MonthlySummary summary = getSummarySummary(12);

        assertEquals(new BigDecimal("5000.00"), summary.getFunds());
        assertEquals(new BigDecimal("1000.00"), summary.getSavings());
        assertEquals(new BigDecimal("1850.00"), summary.getFixedCosts()); // 1500 + 350
        assertEquals(new BigDecimal("285.00"), summary.getSpent()); // 220 + 65
        assertEquals(new BigDecimal("1865.00"), summary.getAvailable()); // 5000 - 1000 - 1850 - 285
    }

    private void verifyDailyLimitMidMonthAndEndOfMonth() {
        // Verify that daily limit is properly calculated
        // Detailed daily limit calculations are tested in dedicated test methods:
        // - testSummaryMidMonthCalculations
        // - testSummaryMidMonthMultipleMonths  
        // - testSummaryEndOfMonthComparison
        // Here we verify the summaries have daily limit values calculated
        
        // January
        MonthlySummary januarySummary = getSummarySummary(1);
        assertNotNull(januarySummary.getDailyLimit(), "January should have a daily limit");
        assertNotNull(januarySummary.getAvailable(), "January should have available amount");
        
        // February
        MonthlySummary februarySummary = getSummarySummary(2);
        assertNotNull(februarySummary.getDailyLimit(), "February should have a daily limit");
        assertEquals(new BigDecimal("1900.00"), februarySummary.getAvailable(), "February available");
        
        // March
        MonthlySummary marchSummary = getSummarySummary(3);
        assertNotNull(marchSummary.getDailyLimit(), "March should have a daily limit");
        assertEquals(new BigDecimal("1910.00"), marchSummary.getAvailable(), "March available");
        
        // July (after rate change)
        MonthlySummary julySummary = getSummarySummary(7);
        assertNotNull(julySummary.getDailyLimit(), "July should have a daily limit");
        assertEquals(new BigDecimal("1627.00"), julySummary.getAvailable(), "July available with new insurance rate");
    }

    @Test
    void testSummaryMidMonthCalculations() {
        // Verify that daily limit changes correctly throughout the month
        // as remaining days decrease AND as expenses accumulate
        
        // Setup
        setupYearlyBudget();
        
        // March: rent 1500 + car 350 = 1850 fixed costs
        // Expenses on: March 3, 18, 20
        // Fixed costs remain constant, but spent increases as we move through the month
        
        // Test on March 1 (first day): 31 remaining days, NO expenses yet
        // Available: 5000 - 1000 - 1850 - 0 = 2150
        // Daily limit: 2150 / 31 = 69.35
        MonthlySummary march1Summary = getSummarySummaryForDate(3, 1);
        assertEquals(new BigDecimal("2150.00"), march1Summary.getAvailable());
        BigDecimal march1DailyLimit = march1Summary.getDailyLimit();
        assertEquals(new BigDecimal("69.35"), march1DailyLimit, "March 1: 2150 / 31 = 69.35");
        
        // Test on March 15 (mid-month): 17 remaining days, only March 3 expense
        // Available: 5000 - 1000 - 1850 - 160 = 1990
        // Daily limit: 1990 / 17 = 117.06
        MonthlySummary march15Summary = getSummarySummaryForDate(3, 15);
        assertEquals(new BigDecimal("1990.00"), march15Summary.getAvailable());
        BigDecimal march15DailyLimit = march15Summary.getDailyLimit();
        assertEquals(new BigDecimal("117.06"), march15DailyLimit, "March 15: 1990 / 17 = 117.06");
        
        // Test on March 31 (last day): 1 remaining day, all expenses accumulated
        // Available: 5000 - 1000 - 1850 - 240 = 1910
        // Daily limit: 1910 / 1 = 1910.00
        MonthlySummary march31Summary = getSummarySummaryForDate(3, 31);
        assertEquals(new BigDecimal("1910.00"), march31Summary.getAvailable());
        BigDecimal march31DailyLimit = march31Summary.getDailyLimit();
        assertEquals(new BigDecimal("1910.00"), march31DailyLimit, "March 31: 1910 / 1 = 1910.00");
        
        // Verify daily limits increase as we approach end of month
        // (even though available decreases due to more expenses)
        assertTrue(march15DailyLimit.compareTo(march1DailyLimit) > 0);
        assertTrue(march31DailyLimit.compareTo(march15DailyLimit) > 0);
        
        // On last day, daily limit should equal available amount
        assertEquals(march31Summary.getAvailable(), march31DailyLimit);
    }

    @Test
    void testSummaryMidMonthMultipleMonths() {
        // Verify mid-month calculations work correctly for different months
        // with different numbers of days and expenses that accumulate during the month
        
        // Setup
        setupYearlyBudget();
        
        // February (28 days) on Feb 14: 15 remaining days
        // Feb: rent 1500 + car 350 = 1850, expenses on Feb 8, 12 (both before Feb 14)
        // Available: 5000 - 1000 - 1850 - (200 + 50) = 1900
        // Daily limit: 1900 / 15 = 126.67
        MonthlySummary feb14Summary = getSummarySummaryForDate(2, 14);
        assertEquals(new BigDecimal("1900.00"), feb14Summary.getAvailable());
        assertEquals(new BigDecimal("126.67"), feb14Summary.getDailyLimit(), "Feb 14: 1900 / 15 = 126.67");
        
        // April (30 days) on Apr 15: 16 remaining days
        // April: rent 1500 + car 350 + insurance 200 = 2050, expenses on Apr 5, 14 (both before Apr 15)
        // Available: 5000 - 1000 - 2050 - (180 + 48) = 1722
        // Daily limit: 1722 / 16 = 107.62
        MonthlySummary apr15Summary = getSummarySummaryForDate(4, 15);
        assertEquals(new BigDecimal("1722.00"), apr15Summary.getAvailable());
        assertEquals(new BigDecimal("107.62"), apr15Summary.getDailyLimit(), "Apr 15: 1722 / 16 = 107.62");
        
        // July (31 days) on Jul 15: 17 remaining days
        // Note: This test doesn't include the rate change that happens in testCompleteYearlyBudgetWithCyclicExpensesAndRandomExpenses
        // So insurance rate is still the initial 200 (not the new 250)
        // July: rent 1500 + car 350 + insurance 200 = 2050, expenses on Jul 8 only (17, 22 are after)
        // Available: 5000 - 1000 - 2050 - 175 = 1775
        // Daily limit: 1775 / 17 = 104.41
        MonthlySummary jul15Summary = getSummarySummaryForDate(7, 15);
        assertEquals(new BigDecimal("1775.00"), jul15Summary.getAvailable());
        assertEquals(new BigDecimal("104.41"), jul15Summary.getDailyLimit(), "Jul 15: 1775 / 17 = 104.41");
    }

    @Test
    void testSummaryEndOfMonthComparison() {
        // Verify end-of-month calculations with day-based expense filtering
        // Daily limit increases as remaining days decrease, even as available decreases from accumulated expenses
        
        // Setup
        setupYearlyBudget();
        
        // January: rent 1500 + car 350 + insurance 200 = 2050
        // Expenses occur on: Jan 5, 10, 15
        
        // Get summaries at different points for the same month
        MonthlySummary jan1Summary = getSummarySummaryForDate(1, 1);   // 31 remaining days, no expenses yet
        MonthlySummary jan15Summary = getSummarySummaryForDate(1, 15);  // 17 remaining days, all expenses accumulated
        MonthlySummary jan31Summary = getSummarySummaryForDate(1, 31);  // 1 remaining day, all expenses accumulated
        
        // Jan 1: No expenses yet
        // Available: 5000 - 1000 - 2050 - 0 = 1950
        // Daily limit: 1950 / 31 = 62.90
        assertEquals(new BigDecimal("1950.00"), jan1Summary.getAvailable(), "Jan 1 available");
        assertEquals(new BigDecimal("62.90"), jan1Summary.getDailyLimit(), "Jan 1: 1950 / 31 = 62.90");
        
        // Jan 15: All expenses on or before Jan 15 (Jan 5, 10, 15)
        // Available: 5000 - 1000 - 2050 - (150 + 45 + 30) = 1725
        // Daily limit: 1725 / 17 = 101.47
        assertEquals(new BigDecimal("1725.00"), jan15Summary.getAvailable(), "Jan 15 available");
        assertEquals(new BigDecimal("101.47"), jan15Summary.getDailyLimit(), "Jan 15: 1725 / 17 = 101.47");
        
        // Jan 31: All expenses (same as Jan 15)
        // Available: 5000 - 1000 - 2050 - 225 = 1725
        // Daily limit: 1725 / 1 = 1725.00
        assertEquals(new BigDecimal("1725.00"), jan31Summary.getAvailable(), "Jan 31 available");
        assertEquals(new BigDecimal("1725.00"), jan31Summary.getDailyLimit(), "Jan 31: 1725 / 1 = 1725.00");
        
        // Verify daily limits increase as we approach end of month
        assertTrue(jan15Summary.getDailyLimit().compareTo(jan1Summary.getDailyLimit()) > 0);
        assertTrue(jan31Summary.getDailyLimit().compareTo(jan15Summary.getDailyLimit()) > 0);
    }

    private MonthlySummary getSummarySummaryForDate(int month, int day) {
        YearMonth yearMonth = YearMonth.of(2025, month);
        AccountingMonth accountingMonth = new AccountingMonth(yearMonth);
        LocalDate date = LocalDate.of(2025, month, day);
        
        MonthlyFunds funds = monthlyFundsRepository.findByYearAndMonth(2025, month).orElseThrow();
        MonthlySavings savings = MonthlySavings.builder().amount(new BigDecimal("1000.00")).build();
        List<CyclicExpense> cyclicExpenses = cyclicExpenseRepository.findAll();
        List<Expense> expenses = expenseRepository.findBySpentAtBetween(
                yearMonth.atDay(1),
                yearMonth.atEndOfMonth()
        );

        return monthlySummaryService.calculate(accountingMonth, funds, savings, cyclicExpenses, expenses, date);
    }
}
