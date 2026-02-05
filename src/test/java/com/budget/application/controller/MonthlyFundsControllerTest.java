package com.budget.application.controller;

import com.budget.domain.MonthlyFunds;
import com.budget.infrastructure.repository.MonthlyFundsRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class MonthlyFundsControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MonthlyFundsRepository repository;

    private MonthlyFunds monthlyFunds;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        repository.deleteAll();
        monthlyFunds = MonthlyFunds.builder()
                .year(2026)
                .month(2)
                .amount(new BigDecimal("5000.00"))
                .build();
    }

    @Test
    void testCreateMonthlyFunds() throws Exception {
        MonthlyFunds toCreate = MonthlyFunds.builder()
                .year(2026)
                .month(2)
                .amount(new BigDecimal("5000.00"))
                .build();

        mockMvc.perform(post("/v1/monthly-funds")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toCreate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.month").value(2))
                .andExpect(jsonPath("$.amount").value(5000.00));
    }

    @Test
    void testGetAllMonthlyFunds() throws Exception {
        MonthlyFunds saved = repository.save(monthlyFunds);

        mockMvc.perform(get("/v1/monthly-funds")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].year").value(2026));
    }

    @Test
    void testGetByYearAndMonth() throws Exception {
        MonthlyFunds saved = repository.save(monthlyFunds);

        mockMvc.perform(get("/v1/monthly-funds/2026/2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.month").value(2));
    }

    @Test
    void testGetByYearAndMonthNotFound() throws Exception {
        mockMvc.perform(get("/v1/monthly-funds/2026/3")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetByYear() throws Exception {
        MonthlyFunds saved = repository.save(monthlyFunds);

        mockMvc.perform(get("/v1/monthly-funds/2026")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].year").value(2026));
    }

    @Test
    void testDeleteMonthlyFunds() throws Exception {
        MonthlyFunds saved = repository.save(monthlyFunds);

        mockMvc.perform(delete("/v1/monthly-funds/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
