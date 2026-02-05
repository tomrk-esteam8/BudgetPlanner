package com.budget.application.dto;

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
public class CreateCyclicExpenseRequest {
    private String name;
    private int cycleInterval;
    private Integer totalCycles;
    private boolean active;
    private BigDecimal initialAmount;
    private LocalDate validFrom;
}
