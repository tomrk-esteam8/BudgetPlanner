package com.budget.application.controller;

import com.budget.application.service.DailyLimitCalculator;
import com.budget.application.service.MonthlySummaryService;
import com.budget.domain.*;
import com.budget.infrastructure.repository.MonthlyFundsRepository;
import com.budget.infrastructure.repository.MonthlySavingsRepository;
import com.budget.infrastructure.repository.CyclicExpenseRepository;
import com.budget.infrastructure.repository.ExpenseRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/v1/summary")
@RequiredArgsConstructor
@Tag(name = "Monthly Summary", description = "Calculate and retrieve monthly budget summaries")
public class MonthlySummaryController {

    private final MonthlySummaryService summaryService;
    private final DailyLimitCalculator dailyLimitCalculator;
    private final MonthlyFundsRepository fundsRepository;
    private final MonthlySavingsRepository savingsRepository;
    private final CyclicExpenseRepository cyclicExpenseRepository;
    private final ExpenseRepository expenseRepository;

    @GetMapping
    @Operation(summary = "Get monthly summary", description = "Calculate and retrieve summary for a specific or current date. By default uses today's date")
    @Parameter(name = "year", description = "Year (required if month/day provided)", example = "2026")
    @Parameter(name = "month", description = "Month 1-12 (required if year or day provided)", example = "2")
    @Parameter(name = "day", description = "Day of month 1-31 (optional, defaults to 1 if year/month provided)", example = "17", required = false)
    @ApiResponse(responseCode = "200", description = "Monthly summary calculated")
    public ResponseEntity<MonthlySummary> getSummary(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day) {
        
        LocalDate requestDate;
        
        // If no parameters provided, use today's date
        if (year == null && month == null && day == null) {
            requestDate = LocalDate.now();
        } else {
            // If any date component is provided, year and month are required
            if (year == null || month == null) {
                throw new IllegalArgumentException("If specifying a date, both year and month are required");
            }
            // Day defaults to 1 if not provided
            int dayOfMonth = day != null ? day : 1;
            try {
                requestDate = LocalDate.of(year, month, dayOfMonth);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid date: year=" + year + ", month=" + month + ", day=" + dayOfMonth, e);
            }
        }
        
        YearMonth yearMonth = YearMonth.from(requestDate);
        AccountingMonth accountingMonth = new AccountingMonth(yearMonth);

        MonthlyFunds funds = fundsRepository.findByYearAndMonth(yearMonth.getYear(), yearMonth.getMonthValue())
                .orElse(null);
        MonthlySavings savings = savingsRepository.findAll().stream()
                .findFirst()
                .orElse(null);
        List<CyclicExpense> cyclicExpenses = cyclicExpenseRepository.findAll();
        List<Expense> expenses = expenseRepository.findAll();

        MonthlySummary summary = summaryService.calculate(
                accountingMonth,
                funds,
                savings,
                cyclicExpenses,
                expenses,
                requestDate
        );

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/daily-limit")
    @Operation(
        summary = "Calculate daily limit",
        description = "Calculate how much can be spent per day for remaining days in the month from a specific date"
    )
    @Parameter(name = "year", description = "Year", example = "2026", required = true)
    @Parameter(name = "month", description = "Month (1-12)", example = "3", required = true)
    @Parameter(name = "date", description = "Day of month (1-31). If not provided, uses today's date", example = "17", required = false)
    @ApiResponse(responseCode = "200", description = "Daily limit calculated")
    public ResponseEntity<DailyLimitResponse> getDailyLimit(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) Integer date) {

        LocalDate requestDate = date != null ?
                LocalDate.of(year, month, date) :
                LocalDate.now();

        // Validate the date is in the requested month
        if (requestDate.getYear() != year || requestDate.getMonthValue() != month) {
            requestDate = LocalDate.of(year, month, date != null ? date : LocalDate.now().getDayOfMonth());
        }

        // Get month data
        YearMonth yearMonth = YearMonth.of(year, month);
        AccountingMonth accountingMonth = new AccountingMonth(yearMonth);
        
        MonthlyFunds funds = fundsRepository.findByYearAndMonth(year, month)
                .orElse(null);
        MonthlySavings savings = savingsRepository.findAll().stream()
                .findFirst()
                .orElse(null);
        List<CyclicExpense> cyclicExpenses = cyclicExpenseRepository.findAll();
        List<Expense> expenses = expenseRepository.findAll();

        // Calculate available amount
        MonthlySummary summary = summaryService.calculate(
                accountingMonth,
                funds,
                savings,
                cyclicExpenses,
                expenses
        );

        // Calculate daily limit based on remaining days from the request date
        BigDecimal dailyLimit = dailyLimitCalculator.calculateFromDate(summary.getAvailable(), requestDate);

        int daysInMonth = accountingMonth.daysInMonth();
        int dayOfMonth = requestDate.getDayOfMonth();
        int remainingDays = daysInMonth - dayOfMonth + 1;

        return ResponseEntity.ok(new DailyLimitResponse(
                requestDate,
                summary.getAvailable(),
                remainingDays,
                daysInMonth,
                dailyLimit
        ));
    }

    /**
     * DTO for daily limit response
     */
    public record DailyLimitResponse(
            LocalDate requestDate,
            BigDecimal available,
            int remainingDays,
            int totalDaysInMonth,
            BigDecimal dailyLimit
    ) {}
}
