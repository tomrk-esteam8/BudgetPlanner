package com.budget.application.controller;

import com.budget.domain.Expense;
import com.budget.domain.MonthlyFunds;
import com.budget.domain.MonthlySavings;
import com.budget.infrastructure.repository.ExpenseRepository;
import com.budget.infrastructure.repository.MonthlyFundsRepository;
import com.budget.infrastructure.repository.MonthlySavingsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class UiSummaryExpensesIntegrationTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MonthlyFundsRepository fundsRepository;

    @Autowired
    private MonthlySavingsRepository savingsRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        expenseRepository.deleteAll();
        fundsRepository.deleteAll();
        savingsRepository.deleteAll();
    }

    @Test
    void testSummaryAndExpensesEndpointsForUi() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate expenseDate = today.minusDays(1);

        MonthlyFunds funds = MonthlyFunds.builder()
                .year(today.getYear())
                .month(today.getMonthValue())
                .amount(new BigDecimal("5000.00"))
                .build();
        fundsRepository.save(funds);

        MonthlySavings savings = MonthlySavings.builder()
                .amount(new BigDecimal("1000.00"))
                .build();
        savingsRepository.save(savings);

        Expense expense = Expense.builder()
                .amount(new BigDecimal("100.00"))
                .category("Groceries")
                .spentAt(expenseDate)
                .build();
        expenseRepository.save(expense);

        mockMvc.perform(get("/v1/summary")
                .param("year", String.valueOf(today.getYear()))
                .param("month", String.valueOf(today.getMonthValue()))
                .param("day", String.valueOf(today.getDayOfMonth()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value(today.toString()))
                .andExpect(jsonPath("$.funds").value(5000.00))
                .andExpect(jsonPath("$.savings").value(1000.00))
                .andExpect(jsonPath("$.spent").value(100.00));

        mockMvc.perform(get("/v1/expenses")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("Groceries"))
                .andExpect(jsonPath("$[0].amount").value(100.00));
    }
}
