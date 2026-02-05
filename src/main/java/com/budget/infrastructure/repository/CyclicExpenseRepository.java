package com.budget.infrastructure.repository;

import com.budget.domain.CyclicExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CyclicExpenseRepository extends JpaRepository<CyclicExpense, UUID> {
    List<CyclicExpense> findByActive(boolean active);
}
