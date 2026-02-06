package com.budget.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cyclic_expense_rates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "cyclicExpense")
public class CyclicExpenseRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cyclic_expense_id", nullable = false)
    @JsonBackReference
    private CyclicExpense cyclicExpense;

    @Column(nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Column(nullable = false)
    @NotNull(message = "Valid from date cannot be null")
    @PastOrPresent(message = "Valid from date cannot be in the future")
    private LocalDate validFrom;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    public BigDecimal amount() {
        return this.amount;
    }

    public LocalDate getValidFrom() {
        return this.validFrom;
    }

    public boolean isActive() {
        return this.active;
    }
}
