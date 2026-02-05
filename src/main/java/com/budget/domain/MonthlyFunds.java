package com.budget.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;

@Entity
@Table(name = "monthly_funds")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyFunds {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "year_number", nullable = false)
    private int year;

    @Column(name = "month_number", nullable = false)
    private int month;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    public YearMonth period() {
        return YearMonth.of(this.year, this.month);
    }

    public BigDecimal amount() {
        return this.amount;
    }
}
