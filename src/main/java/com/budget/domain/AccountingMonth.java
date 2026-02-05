package com.budget.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.YearMonth;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountingMonth {

    private YearMonth yearMonth;

    public int daysInMonth() {
        return this.yearMonth.lengthOfMonth();
    }

    public LocalDate firstDay() {
        return this.yearMonth.atDay(1);
    }

    public LocalDate lastDay() {
        return this.yearMonth.atEndOfMonth();
    }
}
