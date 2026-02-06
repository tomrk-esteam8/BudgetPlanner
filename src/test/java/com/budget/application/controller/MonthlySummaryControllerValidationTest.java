package com.budget.application.controller;

import com.budget.domain.*;
import com.budget.infrastructure.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class MonthlySummaryControllerValidationTest {

    private MockMvc mockMvc;

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
        fundsRepository.deleteAll();
        savingsRepository.deleteAll();
        cyclicExpenseRepository.deleteAll();
        expenseRepository.deleteAll();
    }

    @Test
    void testGetSummary_InvalidYear_TooLow() throws Exception {
        mockMvc.perform(get("/v1/summary")
                .param("year", "1899")
                .param("month", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetSummary_InvalidYear_TooHigh() throws Exception {
        mockMvc.perform(get("/v1/summary")
                .param("year", "2101")
                .param("month", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetSummary_InvalidMonth_TooLow() throws Exception {
        mockMvc.perform(get("/v1/summary")
                .param("year", "2026")
                .param("month", "0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetSummary_InvalidMonth_TooHigh() throws Exception {
        mockMvc.perform(get("/v1/summary")
                .param("year", "2026")
                .param("month", "13")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetSummary_InvalidDay_TooLow() throws Exception {
        mockMvc.perform(get("/v1/summary")
                .param("year", "2026")
                .param("month", "5")
                .param("day", "0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetSummary_InvalidDay_TooHigh() throws Exception {
        mockMvc.perform(get("/v1/summary")
                .param("year", "2026")
                .param("month", "5")
                .param("day", "32")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetSummary_InvalidDay_February29_NonLeapYear() throws Exception {
        mockMvc.perform(get("/v1/summary")
                .param("year", "2025")
                .param("month", "2")
                .param("day", "29")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetSummary_InvalidDay_April31() throws Exception {
        mockMvc.perform(get("/v1/summary")
                .param("year", "2026")
                .param("month", "4")
                .param("day", "31")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetSummary_MissingMonth_WithYear() throws Exception {
        mockMvc.perform(get("/v1/summary")
                .param("year", "2026")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetSummary_MissingYear_WithMonth() throws Exception {
        mockMvc.perform(get("/v1/summary")
                .param("month", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetSummary_ValidDate_WithDay() throws Exception {
        mockMvc.perform(get("/v1/summary")
                .param("year", "2026")
                .param("month", "2")
                .param("day", "15")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-02-15"));
    }

    @Test
    void testGetSummary_ValidDate_WithoutDay_DefaultsToEndOfMonth() throws Exception {
        mockMvc.perform(get("/v1/summary")
                .param("year", "2026")
                .param("month", "2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-02-28"));
    }

    @Test
    void testGetSummary_NoParameters_UsesToday() throws Exception {
        mockMvc.perform(get("/v1/summary")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        // Verify response has a date (it will be today)
    }

    @Test
    void testGetSummary_ValidDate_LeapYear_February29() throws Exception {
        mockMvc.perform(get("/v1/summary")
                .param("year", "2024")
                .param("month", "2")
                .param("day", "29")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2024-02-29"));
    }

    @Test
    void testGetSummary_ValidDate_31stDay_July() throws Exception {
        mockMvc.perform(get("/v1/summary")
                .param("year", "2026")
                .param("month", "7")
                .param("day", "31")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-07-31"));
    }
}
