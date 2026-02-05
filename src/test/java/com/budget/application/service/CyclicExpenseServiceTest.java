package com.budget.application.service;

import com.budget.application.dto.CreateCyclicExpenseRequest;
import com.budget.domain.CyclicExpense;
import com.budget.domain.CyclicExpenseRate;
import com.budget.infrastructure.repository.CyclicExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CyclicExpenseServiceTest {

    @Mock
    private CyclicExpenseRepository repository;

    @InjectMocks
    private CyclicExpenseService service;

    private CyclicExpense cyclicExpense;
    private UUID expenseId;

    @BeforeEach
    void setUp() {
        expenseId = UUID.randomUUID();
        cyclicExpense = CyclicExpense.builder()
                .id(expenseId)
                .name("Monthly Rent")
                .cycleInterval(1)
                .totalCycles(12)
                .active(true)
                .build();
    }

    @Test
    void testSaveCyclicExpense() {
        when(repository.save(any(CyclicExpense.class))).thenReturn(cyclicExpense);

        CyclicExpense result = service.save(cyclicExpense);

        assertNotNull(result);
        assertEquals(expenseId, result.getId());
        assertEquals("Monthly Rent", result.getName());
        assertEquals(1, result.getCycleInterval());
        assertTrue(result.isActive());
        verify(repository, times(1)).save(any(CyclicExpense.class));
    }

    @Test
    void testCreateWithInitialRate() {
        CreateCyclicExpenseRequest request = CreateCyclicExpenseRequest.builder()
                .name("Monthly Rent")
                .cycleInterval(1)
                .totalCycles(12)
                .active(true)
                .initialAmount(new BigDecimal("1500.00"))
                .validFrom(LocalDate.of(2026, 1, 1))
                .build();

        CyclicExpense expectedExpense = CyclicExpense.builder()
                .id(UUID.randomUUID())
                .name("Monthly Rent")
                .cycleInterval(1)
                .totalCycles(12)
                .active(true)
                .build();

        when(repository.save(any(CyclicExpense.class))).thenAnswer(invocation -> {
            CyclicExpense expense = invocation.getArgument(0);
            expense.setId(expectedExpense.getId());
            return expense;
        });

        CyclicExpense result = service.createWithInitialRate(request);

        assertNotNull(result);
        assertEquals("Monthly Rent", result.getName());
        assertEquals(1, result.getRates().size());
        assertEquals(new BigDecimal("1500.00"), result.getRates().iterator().next().getAmount());
        verify(repository, times(1)).save(any(CyclicExpense.class));
    }

    @Test
    void testFindById() {
        when(repository.findById(expenseId)).thenReturn(Optional.of(cyclicExpense));

        Optional<CyclicExpense> result = service.findById(expenseId);

        assertTrue(result.isPresent());
        assertEquals(expenseId, result.get().getId());
        assertEquals("Monthly Rent", result.get().getName());
        verify(repository, times(1)).findById(expenseId);
    }

    @Test
    void testFindByIdNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

        Optional<CyclicExpense> result = service.findById(nonExistentId);

        assertFalse(result.isPresent());
        verify(repository, times(1)).findById(nonExistentId);
    }

    @Test
    void testFindAll() {
        CyclicExpense expense2 = CyclicExpense.builder()
                .id(UUID.randomUUID())
                .name("Car Insurance")
                .cycleInterval(3)
                .totalCycles(4)
                .active(true)
                .build();

        List<CyclicExpense> expensesList = Arrays.asList(cyclicExpense, expense2);
        when(repository.findAll()).thenReturn(expensesList);

        List<CyclicExpense> result = service.findAll();

        assertEquals(2, result.size());
        assertEquals("Monthly Rent", result.get(0).getName());
        assertEquals("Car Insurance", result.get(1).getName());
        verify(repository, times(1)).findAll();
    }

    @Test
    void testFindActiveExpenses() {
        CyclicExpense inactiveExpense = CyclicExpense.builder()
                .id(UUID.randomUUID())
                .name("Old Expense")
                .cycleInterval(1)
                .active(false)
                .build();

        List<CyclicExpense> activeExpenses = Arrays.asList(cyclicExpense);
        when(repository.findByActive(true)).thenReturn(activeExpenses);

        List<CyclicExpense> result = service.findActiveExpenses();

        assertEquals(1, result.size());
        assertTrue(result.get(0).isActive());
        verify(repository, times(1)).findByActive(true);
    }

    @Test
    void testDeleteCyclicExpense() {
        service.delete(expenseId);

        verify(repository, times(1)).deleteById(expenseId);
    }

    @Test
    void testAddNewRateDeactivatesPreviousRates() {
        CyclicExpense expenseWithRate = CyclicExpense.builder()
                .id(expenseId)
                .name("Monthly Rent")
                .cycleInterval(1)
                .totalCycles(12)
                .active(true)
                .build();

        CyclicExpenseRate oldRate = CyclicExpenseRate.builder()
                .id(1L)
                .amount(new BigDecimal("1500.00"))
                .validFrom(LocalDate.of(2026, 1, 1))
                .active(true)
                .build();

        expenseWithRate.getRates().add(oldRate);

        CyclicExpenseRate newRate = CyclicExpenseRate.builder()
                .amount(new BigDecimal("1600.00"))
                .validFrom(LocalDate.of(2026, 6, 1))
                .build();

        when(repository.findById(expenseId)).thenReturn(Optional.of(expenseWithRate));
        when(repository.save(any(CyclicExpense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CyclicExpense result = service.addNewRate(expenseId, newRate);

        // Old rate should be deactivated
        CyclicExpenseRate oldRateInResult = result.getRates().stream()
                .filter(r -> r.getValidFrom().equals(LocalDate.of(2026, 1, 1)))
                .findFirst()
                .orElse(null);
        assertNotNull(oldRateInResult);
        assertFalse(oldRateInResult.isActive());

        // New rate should be active
        CyclicExpenseRate newRateInResult = result.getRates().stream()
                .filter(r -> r.getValidFrom().equals(LocalDate.of(2026, 6, 1)))
                .findFirst()
                .orElse(null);
        assertNotNull(newRateInResult);
        assertTrue(newRateInResult.isActive());

        assertEquals(2, result.getRates().size());
    }
}
