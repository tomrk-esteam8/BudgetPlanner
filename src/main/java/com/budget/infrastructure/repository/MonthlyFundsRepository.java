package com.budget.infrastructure.repository;

import com.budget.domain.MonthlyFunds;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyFundsRepository extends JpaRepository<MonthlyFunds, Long> {
    List<MonthlyFunds> findByYearAndMonth(int year, int month);

    Optional<MonthlyFunds> findTopByYearAndMonthOrderByIdDesc(int year, int month);

    List<MonthlyFunds> findByYear(int year);
}
