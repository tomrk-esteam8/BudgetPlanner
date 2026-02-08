package com.budget.application.controller;

import com.budget.domain.Expense;
import com.budget.infrastructure.repository.ExpenseRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping("/v1/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Manage individual expenses")
public class ExpenseController {

    private final ExpenseRepository repository;

    @PostMapping
    @Operation(summary = "Create a new expense", description = "Add a new expense entry")
    @ApiResponse(responseCode = "200", description = "Expense created successfully")
    public ResponseEntity<Expense> create(@RequestBody Expense expense) {
        return ResponseEntity.ok(repository.save(expense));
    }

    @GetMapping
    @Operation(summary = "Get all expenses", description = "Retrieve all expense entries")
    @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully")
    public ResponseEntity<List<Expense>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping(params = {"page", "size"})
    @Operation(summary = "Get expenses page", description = "Retrieve expenses with pagination")
    @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully")
    public ResponseEntity<Page<Expense>> getPage(
            @RequestParam int page,
            @RequestParam int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("spentAt").descending());
        return ResponseEntity.ok(repository.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get expense by ID", description = "Retrieve a specific expense by its ID")
    @Parameter(name = "id", description = "Expense ID", example = "1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Expense found"),
            @ApiResponse(responseCode = "404", description = "Expense not found")
    })
    public ResponseEntity<Expense> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get expenses by category", description = "Retrieve all expenses in a specific category")
    @Parameter(name = "category", description = "Expense category", example = "Food")
    @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully")
    public ResponseEntity<List<Expense>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(repository.findByCategory(category));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete expense", description = "Delete a specific expense by its ID")
    @Parameter(name = "id", description = "Expense ID", example = "1")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Expense deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Expense not found")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update expense", description = "Update a specific expense by its ID")
    @Parameter(name = "id", description = "Expense ID", example = "1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Expense updated successfully"),
            @ApiResponse(responseCode = "404", description = "Expense not found")
    })
    public ResponseEntity<Expense> update(@PathVariable Long id, @RequestBody Expense request) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setAmount(request.getAmount());
                    existing.setCategory(request.getCategory());
                    existing.setSpentAt(request.getSpentAt());
                    return ResponseEntity.ok(repository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
