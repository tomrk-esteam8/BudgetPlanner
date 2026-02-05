package com.budget.application.service;

import com.budget.domain.MonthlyFunds;
import com.budget.infrastructure.repository.MonthlyFundsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MonthlyFundsService {

    private final MonthlyFundsRepository repository;

    public MonthlyFunds save(MonthlyFunds monthlyFunds) {
        return repository.save(monthlyFunds);
    }

    public Optional<MonthlyFunds> findByYearAndMonth(int year, int month) {
        return repository.findByYearAndMonth(year, month);
    }

    public List<MonthlyFunds> findByYear(int year) {
        return repository.findByYear(year);
    }

    public List<MonthlyFunds> findAll() {
        return repository.findAll();
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
