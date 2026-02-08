package com.budget.application.service;

import com.budget.domain.MonthlyFunds;
import com.budget.infrastructure.repository.MonthlyFundsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MonthlyFundsService {

    private final MonthlyFundsRepository repository;

    public MonthlyFunds save(MonthlyFunds monthlyFunds) {
        List<MonthlyFunds> existing = repository.findByYearAndMonth(
                monthlyFunds.getYear(),
                monthlyFunds.getMonth()
        );

        if (!existing.isEmpty()) {
            throw new IllegalArgumentException(
                String.format(
                    "Monthly funds already exist for %d-%02d",
                    monthlyFunds.getYear(),
                    monthlyFunds.getMonth()
                )
            );
        }

        return repository.save(monthlyFunds);
    }

    public Optional<MonthlyFunds> findByYearAndMonth(int year, int month) {
        return repository.findTopByYearAndMonthOrderByIdDesc(year, month);
    }

    public List<MonthlyFunds> findByYear(int year) {
        return repository.findByYear(year);
    }

    public List<MonthlyFunds> findAll() {
        return repository.findAll();
    }

    public Page<MonthlyFunds> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public MonthlyFunds update(Long id, MonthlyFunds request) {
        MonthlyFunds existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Monthly funds not found"));

        List<MonthlyFunds> dupes = repository.findByYearAndMonth(request.getYear(), request.getMonth());
        boolean hasOther = dupes.stream().anyMatch(item -> !item.getId().equals(existing.getId()));
        if (hasOther) {
            throw new IllegalArgumentException(
                    String.format(
                            "Monthly funds already exist for %d-%02d",
                            request.getYear(),
                            request.getMonth()
                    )
            );
        }

        existing.setYear(request.getYear());
        existing.setMonth(request.getMonth());
        existing.setAmount(request.getAmount());
        return repository.save(existing);
    }
}
