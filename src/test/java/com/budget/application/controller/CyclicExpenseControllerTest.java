package com.budget.application.controller;

import com.budget.application.dto.CreateCyclicExpenseRequest;
import com.budget.domain.CyclicExpense;
import com.budget.infrastructure.repository.CyclicExpenseRepository;
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
import java.util.HashSet;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class CyclicExpenseControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CyclicExpenseRepository repository;

    private CyclicExpense cyclicExpense;
    private UUID expenseId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        repository.deleteAll();
        expenseId = UUID.randomUUID();
        cyclicExpense = CyclicExpense.builder()
                .id(expenseId)
                .name("Monthly Rent")
                .cycleInterval(1)
                .totalCycles(12)
                .active(true)
                .rates(new HashSet<>())
                .build();
    }

    @Test
    void testCreateCyclicExpense() throws Exception {
        CreateCyclicExpenseRequest toCreate = CreateCyclicExpenseRequest.builder()
                .name("Monthly Rent")
                .cycleInterval(1)
                .totalCycles(12)
                .active(true)
                .initialAmount(new BigDecimal("1500.00"))
                .validFrom(LocalDate.of(2026, 1, 1))
                .build();

        mockMvc.perform(post("/v1/cyclic-expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toCreate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Monthly Rent"))
                .andExpect(jsonPath("$.cycleInterval").value(1))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.rates.length()").value(1));
    }

            @Test
            void testCreateCyclicExpenseWithoutTotalCycles() throws Exception {
            CreateCyclicExpenseRequest toCreate = CreateCyclicExpenseRequest.builder()
                .name("Streaming")
                .cycleInterval(1)
                .active(true)
                .initialAmount(new BigDecimal("29.99"))
                .validFrom(LocalDate.of(2026, 2, 1))
                .build();

            mockMvc.perform(post("/v1/cyclic-expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toCreate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Streaming"))
                .andExpect(jsonPath("$.cycleInterval").value(1))
                .andExpect(jsonPath("$.rates.length()").value(1));
            }

    @Test
    void testGetAllCyclicExpenses() throws Exception {
        repository.save(cyclicExpense);

        mockMvc.perform(get("/v1/cyclic-expenses")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Monthly Rent"));
    }

    @Test
    void testGetActiveCyclicExpenses() throws Exception {
        repository.save(cyclicExpense);

        mockMvc.perform(get("/v1/cyclic-expenses/active")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void testGetCyclicExpensesPageReturnsTenItems() throws Exception {
        for (int i = 1; i <= 12; i++) {
            repository.save(CyclicExpense.builder()
                    .id(UUID.randomUUID())
                    .name("Expense " + i)
                    .cycleInterval(1)
                    .totalCycles(12)
                    .active(true)
                    .rates(new HashSet<>())
                    .build());
        }

        mockMvc.perform(get("/v1/cyclic-expenses?page=0&size=10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.totalElements").value(12));
    }

    @Test
    void testGetCyclicExpenseById() throws Exception {
        CyclicExpense saved = repository.save(cyclicExpense);

        mockMvc.perform(get("/v1/cyclic-expenses/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Monthly Rent"));
    }

    @Test
    void testGetCyclicExpenseByIdNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/v1/cyclic-expenses/" + nonExistentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteCyclicExpense() throws Exception {
        CyclicExpense saved = repository.save(cyclicExpense);

        mockMvc.perform(delete("/v1/cyclic-expenses/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
