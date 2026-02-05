package com.budget.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "monthly_savings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlySavings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    public BigDecimal amount() {
        return this.amount;
    }
}
