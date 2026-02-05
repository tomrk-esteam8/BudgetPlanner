package com.budget.application.service;

import com.budget.domain.CyclicExpense;
import com.budget.domain.CyclicExpenseRate;
import com.budget.domain.AccountingMonth;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Optional;

@Component
public class CyclicExpenseCalculator {

    public boolean applies(CyclicExpense expense, AccountingMonth month) {
        if (!expense.isActive()) {
            return false;
        }
        
        // Check if the expense has a valid rate for this month
        Optional<CyclicExpenseRate> rate = expense.rateFor(month.getYearMonth());
        if (rate.isEmpty()) {
            return false;
        }
        
        // Check if this month falls within the cycle interval
        return isMonthInCycle(expense, month);
    }

    private boolean isMonthInCycle(CyclicExpense expense, AccountingMonth month) {
        if (expense.getCycleInterval() <= 0) {
            return false;
        }
        
        YearMonth rateStartMonth = YearMonth.from(
            expense.rateFor(month.getYearMonth()).get().getValidFrom()
        );
        
        // Calculate months elapsed since rate started
        long monthsElapsed = java.time.temporal.ChronoUnit.MONTHS.between(
            rateStartMonth,
            month.getYearMonth()
        );
        
        // Check if this month is within the cycle (e.g., every 1 month, every 3 months, etc)
        return monthsElapsed >= 0 && monthsElapsed % expense.getCycleInterval() == 0;
    }

    public BigDecimal amountForMonth(CyclicExpense expense, AccountingMonth month) {
        return expense.rateFor(month.getYearMonth())
                .map(rate -> rate.getAmount())
                .orElse(BigDecimal.ZERO);
    }
}
