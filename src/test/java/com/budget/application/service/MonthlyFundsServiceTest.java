package com.budget.application.service;

import com.budget.domain.MonthlyFunds;
import com.budget.infrastructure.repository.MonthlyFundsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonthlyFundsServiceTest {

    @Mock
    private MonthlyFundsRepository repository;

    @InjectMocks
    private MonthlyFundsService service;

    private MonthlyFunds monthlyFunds;

    @BeforeEach
    void setUp() {
        monthlyFunds = MonthlyFunds.builder()
                .id(1L)
                .year(2026)
                .month(2)
                .amount(new BigDecimal("5000.00"))
                .build();
    }

    @Test
    void testSaveMonthlyFunds() {
        when(repository.findByYearAndMonth(2026, 2)).thenReturn(List.of());
        when(repository.save(any(MonthlyFunds.class))).thenReturn(monthlyFunds);

        MonthlyFunds result = service.save(monthlyFunds);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(2026, result.getYear());
        assertEquals(2, result.getMonth());
        assertEquals(new BigDecimal("5000.00"), result.getAmount());
        verify(repository, times(1)).save(any(MonthlyFunds.class));
    }

    @Test
        void testSaveMonthlyFundsThrowsOnDuplicate() {
        MonthlyFunds existing = MonthlyFunds.builder()
                .id(5L)
                .year(2026)
                .month(2)
                .amount(new BigDecimal("4500.00"))
                .build();

        when(repository.findByYearAndMonth(2026, 2)).thenReturn(List.of(existing));

        IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> service.save(monthlyFunds)
        );

        assertTrue(thrown.getMessage().contains("Monthly funds already exist"));
        verify(repository, never()).save(any(MonthlyFunds.class));
    }

    @Test
    void testFindByYearAndMonth() {
        when(repository.findTopByYearAndMonthOrderByIdDesc(2026, 2))
            .thenReturn(Optional.of(monthlyFunds));

        Optional<MonthlyFunds> result = service.findByYearAndMonth(2026, 2);

        assertTrue(result.isPresent());
        assertEquals(monthlyFunds.getId(), result.get().getId());
        verify(repository, times(1)).findTopByYearAndMonthOrderByIdDesc(2026, 2);
    }

    @Test
    void testFindByYearAndMonthNotFound() {
        when(repository.findTopByYearAndMonthOrderByIdDesc(2026, 3))
            .thenReturn(Optional.empty());

        Optional<MonthlyFunds> result = service.findByYearAndMonth(2026, 3);

        assertFalse(result.isPresent());
        verify(repository, times(1)).findTopByYearAndMonthOrderByIdDesc(2026, 3);
    }

    @Test
    void testFindByYear() {
        MonthlyFunds funds2 = MonthlyFunds.builder()
                .id(2L)
                .year(2026)
                .month(3)
                .amount(new BigDecimal("6000.00"))
                .build();

        List<MonthlyFunds> fundsList = Arrays.asList(monthlyFunds, funds2);
        when(repository.findByYear(2026)).thenReturn(fundsList);

        List<MonthlyFunds> result = service.findByYear(2026);

        assertEquals(2, result.size());
        assertEquals(monthlyFunds.getId(), result.get(0).getId());
        assertEquals(funds2.getId(), result.get(1).getId());
        verify(repository, times(1)).findByYear(2026);
    }

    @Test
    void testFindAll() {
        List<MonthlyFunds> fundsList = Arrays.asList(monthlyFunds);
        when(repository.findAll()).thenReturn(fundsList);

        List<MonthlyFunds> result = service.findAll();

        assertEquals(1, result.size());
        assertEquals(monthlyFunds.getId(), result.get(0).getId());
        verify(repository, times(1)).findAll();
    }

    @Test
    void testDeleteMonthlyFunds() {
        service.delete(1L);

        verify(repository, times(1)).deleteById(1L);
    }
}
