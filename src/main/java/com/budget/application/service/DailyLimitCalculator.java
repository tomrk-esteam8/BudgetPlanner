package com.budget.application.service;

import com.budget.domain.AccountingMonth;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;

@Component
public class DailyLimitCalculator {

    public BigDecimal calculate(BigDecimal available, AccountingMonth month) {
        if (available.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        int daysInMonth = month.daysInMonth();
        return available.divide(
                BigDecimal.valueOf(daysInMonth),
                2,
                RoundingMode.HALF_DOWN
        );
    }

    /**
     * Calculate daily limit based on remaining days in the month from a specific date.
     * @param available The available amount
     * @param date The date to calculate from (e.g., March 17)
     * @return Daily limit = available / remaining days (including current day)
     *         For March 17 in 31-day month: (31 - 17 + 1) = 15 remaining days
     *         Returns 0 if available <= 0
     */
    public BigDecimal calculateFromDate(BigDecimal available, LocalDate date) {
        if (available.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        YearMonth yearMonth = YearMonth.from(date);
        AccountingMonth month = new AccountingMonth(yearMonth);
        
        int daysInMonth = month.daysInMonth();
        int dayOfMonth = date.getDayOfMonth();
        int remainingDays = daysInMonth - dayOfMonth + 1; // +1 to include current day

        return available.divide(
                BigDecimal.valueOf(remainingDays),
                2,
                RoundingMode.HALF_DOWN
        );
    }
}
