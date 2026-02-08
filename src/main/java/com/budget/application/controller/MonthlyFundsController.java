package com.budget.application.controller;

import com.budget.application.service.MonthlyFundsService;
import com.budget.domain.MonthlyFunds;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/monthly-funds")
@RequiredArgsConstructor
@Tag(name = "Monthly Funds", description = "APIs for managing monthly funds")
public class MonthlyFundsController {

    private final MonthlyFundsService service;

    @PostMapping
    @Operation(summary = "Create new monthly funds", description = "Create a new entry for monthly funds")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monthly funds created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    public ResponseEntity<MonthlyFunds> create(@RequestBody MonthlyFunds monthlyFunds) {
        MonthlyFunds saved = service.save(monthlyFunds);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    @Operation(summary = "Get all monthly funds", description = "Retrieve all monthly funds entries")
    @ApiResponse(responseCode = "200", description = "List of all monthly funds")
    public ResponseEntity<List<MonthlyFunds>> getAll() {
        List<MonthlyFunds> funds = service.findAll();
        return ResponseEntity.ok(funds);
    }

    @GetMapping(params = {"page", "size"})
    @Operation(summary = "Get monthly funds page", description = "Retrieve monthly funds with pagination")
    @ApiResponse(responseCode = "200", description = "List of monthly funds")
    public ResponseEntity<Page<MonthlyFunds>> getPage(
            @RequestParam int page,
            @RequestParam int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("year", "month").descending());
        return ResponseEntity.ok(service.findAll(pageable));
    }

    @GetMapping("/{year}/{month}")
    @Operation(summary = "Get funds by year and month", description = "Retrieve monthly funds for a specific year and month")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monthly funds found"),
            @ApiResponse(responseCode = "404", description = "Monthly funds not found")
    })
    public ResponseEntity<MonthlyFunds> getByYearAndMonth(
            @Parameter(description = "Year", example = "2026") @PathVariable int year,
            @Parameter(description = "Month", example = "2") @PathVariable int month) {
        return service.findByYearAndMonth(year, month)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{year}")
    @Operation(summary = "Get funds by year", description = "Retrieve all monthly funds for a specific year")
    @ApiResponse(responseCode = "200", description = "List of monthly funds for the year")
    public ResponseEntity<List<MonthlyFunds>> getByYear(
            @Parameter(description = "Year", example = "2026") @PathVariable int year) {
        List<MonthlyFunds> funds = service.findByYear(year);
        return ResponseEntity.ok(funds);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete monthly funds", description = "Delete a monthly funds entry by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Monthly funds deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Monthly funds not found")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Monthly funds ID", example = "1") @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update monthly funds", description = "Update a monthly funds entry by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monthly funds updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "404", description = "Monthly funds not found")
    })
    public ResponseEntity<MonthlyFunds> update(
            @Parameter(description = "Monthly funds ID", example = "1") @PathVariable Long id,
            @RequestBody MonthlyFunds monthlyFunds) {
        MonthlyFunds saved = service.update(id, monthlyFunds);
        return ResponseEntity.ok(saved);
    }
}
