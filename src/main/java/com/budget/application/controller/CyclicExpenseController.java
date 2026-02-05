package com.budget.application.controller;

import com.budget.application.dto.CreateCyclicExpenseRequest;
import com.budget.application.service.CyclicExpenseService;
import com.budget.domain.CyclicExpense;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/cyclic-expenses")
@RequiredArgsConstructor
@Tag(name = "Cyclic Expenses", description = "APIs for managing cyclic expenses")
public class CyclicExpenseController {

    private final CyclicExpenseService service;

    @PostMapping
    @Operation(summary = "Create new cyclic expense with initial rate", description = "Create a new cyclic expense with an initial rate to define when it starts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cyclic expense created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    public ResponseEntity<CyclicExpense> create(@RequestBody CreateCyclicExpenseRequest request) {
        CyclicExpense saved = service.createWithInitialRate(request);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    @Operation(summary = "Get all cyclic expenses", description = "Retrieve all cyclic expenses")
    @ApiResponse(responseCode = "200", description = "List of all cyclic expenses")
    public ResponseEntity<List<CyclicExpense>> getAll() {
        List<CyclicExpense> expenses = service.findAll();
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active cyclic expenses", description = "Retrieve only active cyclic expenses")
    @ApiResponse(responseCode = "200", description = "List of active cyclic expenses")
    public ResponseEntity<List<CyclicExpense>> getActive() {
        List<CyclicExpense> expenses = service.findActiveExpenses();
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get cyclic expense by ID", description = "Retrieve a specific cyclic expense by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cyclic expense found"),
            @ApiResponse(responseCode = "404", description = "Cyclic expense not found")
    })
    public ResponseEntity<CyclicExpense> getById(
            @Parameter(description = "Cyclic expense ID", example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete cyclic expense", description = "Delete a cyclic expense by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cyclic expense deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Cyclic expense not found")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Cyclic expense ID", example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
