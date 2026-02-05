package com.budget.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlySummary {

    private LocalDate date;
    private BigDecimal funds;
    private BigDecimal savings;
    private BigDecimal fixedCosts;
    private BigDecimal spent;
    private BigDecimal available;
    private BigDecimal dailyLimit;
}
