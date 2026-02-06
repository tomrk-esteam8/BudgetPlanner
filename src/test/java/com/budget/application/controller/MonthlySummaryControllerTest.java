package com.budget.application.controller;

import com.budget.domain.*;
import com.budget.infrastructure.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class MonthlySummaryControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MonthlyFundsRepository fundsRepository;

    @Autowired
    private MonthlySavingsRepository savingsRepository;

    @Autowired
    private CyclicExpenseRepository cyclicExpenseRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        fundsRepository.deleteAll();
        savingsRepository.deleteAll();
        cyclicExpenseRepository.deleteAll();
        expenseRepository.deleteAll();
    }

    @Test
    void testGetMonthlySummaryWithData() throws Exception {
        MonthlyFunds funds = MonthlyFunds.builder()
                .year(2026)
                .month(2)
                .amount(new BigDecimal("5000.00"))
                .build();
        fundsRepository.save(funds);

        MonthlySavings savings = MonthlySavings.builder()
                .amount(new BigDecimal("1000.00"))
                .build();
        savingsRepository.save(savings);

        Expense expense = Expense.builder()
                .amount(new BigDecimal("100.00"))
                .category("Food")
                .spentAt(LocalDate.of(2026, 2, 5))
                .build();
        expenseRepository.save(expense);

        mockMvc.perform(get("/v1/summary")
                .param("year", "2026")
                .param("month", "2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-02-28"))
                .andExpect(jsonPath("$.funds").value(5000.00))
                .andExpect(jsonPath("$.savings").value(1000.00))
                .andExpect(jsonPath("$.spent").value(100.00));
    }

    @Test
    void testGetMonthlySummaryWithoutData() throws Exception {
        mockMvc.perform(get("/v1/summary")
                .param("year", "2026")
                .param("month", "2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-02-28"))
                .andExpect(jsonPath("$.funds").value(0.00))
                .andExpect(jsonPath("$.savings").value(0.00));
    }

    @Test
    void testGetMonthlySummaryWithCyclicExpenses() throws Exception {
        MonthlyFunds funds = MonthlyFunds.builder()
                .year(2026)
                .month(2)
                .amount(new BigDecimal("5000.00"))
                .build();
        fundsRepository.save(funds);

        CyclicExpense cyclicExpense = CyclicExpense.builder()
                .id(UUID.randomUUID())
                .name("Rent")
                .cycleInterval(1)
                .totalCycles(12)
                .active(true)
                .rates(new HashSet<>())
                .build();
        cyclicExpense = cyclicExpenseRepository.save(cyclicExpense);

        CyclicExpenseRate rate = CyclicExpenseRate.builder()
                .amount(new BigDecimal("1500.00"))
                .validFrom(LocalDate.of(2026, 1, 1))
                .cyclicExpense(cyclicExpense)
                .build();
        cyclicExpense.getRates().add(rate);
        cyclicExpenseRepository.save(cyclicExpense);

        mockMvc.perform(get("/v1/summary")
                .param("year", "2026")
                .param("month", "2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-02-28"))
                .andExpect(jsonPath("$.fixedCosts").value(1500.00));
    }
}
