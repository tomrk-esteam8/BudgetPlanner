package com.budget.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "cyclic_expenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "rates")
public class CyclicExpense {

    @Id
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(nullable = false)
    @NotBlank(message = "Cyclic expense name cannot be blank")
    private String name;

    @Column(nullable = false)
    @Min(value = 1, message = "Cycle interval must be at least 1 month")
    private int cycleInterval;

    @Column(nullable = true)
    @Min(value = 1, message = "Total cycles must be at least 1")
    private Integer totalCycles;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "cyclicExpense", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    @Builder.Default
    private Set<CyclicExpenseRate> rates = new HashSet<>();

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }

    public Optional<CyclicExpenseRate> rateFor(YearMonth month) {
        LocalDate endOfMonth = month.atEndOfMonth();
        return this.rates.stream()
                .filter(CyclicExpenseRate::isActive)
            .filter(rate -> !rate.getValidFrom().isAfter(endOfMonth))
                .max((r1, r2) -> r1.getValidFrom().compareTo(r2.getValidFrom()));
    }

    public boolean isActive() {
        return this.active;
    }
}
