package com.budget.application.controller;

import com.budget.domain.Expense;
import com.budget.infrastructure.repository.ExpenseRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class ExpenseControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ExpenseRepository repository;

    private Expense expense;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        repository.deleteAll();
        expense = Expense.builder()
                .amount(new BigDecimal("50.00"))
                .category("Food")
                .spentAt(LocalDate.of(2026, 2, 5))
                .build();
    }

    @Test
    void testCreateExpense() throws Exception {
        Expense toCreate = Expense.builder()
                .amount(new BigDecimal("50.00"))
                .category("Food")
                .spentAt(LocalDate.of(2026, 2, 5))
                .build();

        mockMvc.perform(post("/v1/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toCreate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(50.00))
                .andExpect(jsonPath("$.category").value("Food"));
    }

    @Test
    void testGetAllExpenses() throws Exception {
        Expense saved = repository.save(expense);

        mockMvc.perform(get("/v1/expenses")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("Food"));
    }

    @Test
    void testGetExpenseById() throws Exception {
        Expense saved = repository.save(expense);

        mockMvc.perform(get("/v1/expenses/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(50.00));
    }

    @Test
    void testGetExpenseByIdNotFound() throws Exception {
        mockMvc.perform(get("/v1/expenses/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetExpensesByCategory() throws Exception {
        Expense saved = repository.save(expense);

        mockMvc.perform(get("/v1/expenses/category/Food")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("Food"));
    }

    @Test
    void testDeleteExpense() throws Exception {
        Expense saved = repository.save(expense);

        mockMvc.perform(delete("/v1/expenses/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteExpenseNotFound() throws Exception {
        mockMvc.perform(delete("/v1/expenses/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
