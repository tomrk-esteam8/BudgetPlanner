package com.budget.application.service;

import com.budget.application.dto.CreateCyclicExpenseRequest;
import com.budget.domain.CyclicExpense;
import com.budget.domain.CyclicExpenseRate;
import com.budget.infrastructure.repository.CyclicExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CyclicExpenseService {

    private final CyclicExpenseRepository repository;

    public CyclicExpense save(CyclicExpense cyclicExpense) {
        return repository.save(cyclicExpense);
    }

    public CyclicExpense createWithInitialRate(CreateCyclicExpenseRequest request) {
        CyclicExpense cyclicExpense = CyclicExpense.builder()
                .name(request.getName())
                .cycleInterval(request.getCycleInterval())
                .totalCycles(request.getTotalCycles())
                .active(request.isActive())
                .rates(new HashSet<>())
                .build();

        CyclicExpenseRate initialRate = CyclicExpenseRate.builder()
                .amount(request.getInitialAmount())
                .validFrom(request.getValidFrom())
                .active(true)
                .cyclicExpense(cyclicExpense)
                .build();

        cyclicExpense.getRates().add(initialRate);
        return repository.save(cyclicExpense);
    }

    public CyclicExpense addNewRate(UUID expenseId, CyclicExpenseRate newRate) {
        CyclicExpense expense = repository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Cyclic expense not found"));
        
        // Deactivate all existing rates
        expense.getRates().forEach(rate -> rate.setActive(false));
        
        // Add new active rate
        newRate.setCyclicExpense(expense);
        newRate.setActive(true);
        expense.getRates().add(newRate);
        
        return repository.save(expense);
    }

    public Optional<CyclicExpense> findById(UUID id) {
        return repository.findById(id);
    }

    public List<CyclicExpense> findAll() {
        return repository.findAll();
    }

    public List<CyclicExpense> findActiveExpenses() {
        return repository.findByActive(true);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }
}
