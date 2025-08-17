package lk.acpt.smartbiz.service;

import lk.acpt.smartbiz.dto.ExpenseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ExpenseService {
    ExpenseDto saveExpense(ExpenseDto dto);
    ExpenseDto updateExpense(ExpenseDto dto);
    ExpenseDto deleteExpense(Long id);
    List<ExpenseDto> getAllExpenses();
    ExpenseDto getExpenseById(Long id);
}