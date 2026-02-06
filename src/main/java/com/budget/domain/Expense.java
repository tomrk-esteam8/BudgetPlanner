package com.budget.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Entity
@Table(name = "expenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Column(nullable = false)
    @NotBlank(message = "Category cannot be blank")
    private String category;

    @Column(nullable = false)
    @NotNull(message = "Spent date cannot be null")
    @PastOrPresent(message = "Spent date cannot be in the future")
    private LocalDate spentAt;

    public BigDecimal amount() {
        return this.amount;
    }

    public boolean inMonth(YearMonth month) {
        YearMonth spentMonth = YearMonth.from(this.spentAt);
        return spentMonth.equals(month);
    }
}
