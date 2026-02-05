package com.budget.infrastructure.repository;

import com.budget.domain.MonthlySavings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonthlySavingsRepository extends JpaRepository<MonthlySavings, Long> {
}
