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
public class UpdateCyclicExpenseRequest {
    private String name;
    private Integer cycleInterval;
    private Integer totalCycles;
    private Boolean active;
    private BigDecimal amount;
    private LocalDate validFrom;
}
