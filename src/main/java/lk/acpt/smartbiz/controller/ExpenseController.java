package lk.acpt.smartbiz.controller;

import lk.acpt.smartbiz.dto.ExpenseDto;
import lk.acpt.smartbiz.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/expense")
@PreAuthorize("hasRole('OWNER')")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Autowired
    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseDto> createExpense(@RequestBody ExpenseDto dto) {
        ExpenseDto saved = expenseService.saveExpense(dto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateExpense(@PathVariable Long id, @RequestBody ExpenseDto dto) {
        dto.setId(id);
        ExpenseDto updated = expenseService.updateExpense(dto);
        if (updated != null) return new ResponseEntity<>(updated, HttpStatus.OK);
        return new ResponseEntity<>(Map.of("result", "Expense not found"), HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteExpense(@PathVariable Long id) {
        ExpenseDto deleted = expenseService.deleteExpense(id);
        if (deleted != null) return new ResponseEntity<>(deleted, HttpStatus.OK);
        return new ResponseEntity<>(Map.of("result", "Expense not found"), HttpStatus.NOT_FOUND);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseDto>> getAllExpenses() {
        return new ResponseEntity<>(expenseService.getAllExpenses(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getExpense(@PathVariable Long id) {
        ExpenseDto expense = expenseService.getExpenseById(id);
        if (expense != null) return new ResponseEntity<>(expense, HttpStatus.OK);
        return new ResponseEntity<>(Map.of("result", "Expense not found"), HttpStatus.NOT_FOUND);
    }
}