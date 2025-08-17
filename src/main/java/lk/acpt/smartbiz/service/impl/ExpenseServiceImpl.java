package lk.acpt.smartbiz.service.impl;

import lk.acpt.smartbiz.dto.ExpenseDto;
import lk.acpt.smartbiz.entity.Business;
import lk.acpt.smartbiz.entity.Expense;
import lk.acpt.smartbiz.entity.User;
import lk.acpt.smartbiz.repo.BusinessRepository;
import lk.acpt.smartbiz.repo.ExpenseRepository;
import lk.acpt.smartbiz.repo.UserRepository;
import lk.acpt.smartbiz.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepo;
    private final UserRepository userRepo;
    private final BusinessRepository businessRepo;

    @Autowired
    public ExpenseServiceImpl(ExpenseRepository expenseRepo, UserRepository userRepo, BusinessRepository businessRepo) {
        this.expenseRepo = expenseRepo;
        this.userRepo = userRepo;
        this.businessRepo = businessRepo;
    }

    private Business currentBusiness() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User owner = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("Owner not found"));
        return businessRepo.findByOwner(owner).orElseThrow(() -> new RuntimeException("Business not found"));
    }

    private ExpenseDto toDto(Expense e) {
        return new ExpenseDto(
                e.getExpenseId(),
                e.getDate(),
                e.getAmount(),
                e.getDescription(),
                e.getCategory()
        );
    }

    @Override
    public ExpenseDto saveExpense(ExpenseDto dto) {
        Business business = currentBusiness();
        Expense expense = new Expense();
        expense.setBusiness(business);
        expense.setDate(LocalDateTime.now());
        expense.setAmount(dto.getAmount());
        expense.setDescription(dto.getDescription());
        expense.setCategory(dto.getCategory());
        Expense saved = expenseRepo.save(expense);
        return toDto(saved);
    }

    @Override
    public ExpenseDto updateExpense(ExpenseDto dto) {
        Business business = currentBusiness();
        Optional<Expense> byId = expenseRepo.findById(dto.getId());
        if (byId.isPresent() && byId.get().getBusiness().equals(business)) {
            Expense e = byId.get();
            e.setAmount(dto.getAmount());
            e.setDescription(dto.getDescription());
            e.setCategory(dto.getCategory());
            Expense updated = expenseRepo.save(e);
            return toDto(updated);
        }
        return null;
    }

    @Override
    public ExpenseDto deleteExpense(Long id) {
        Business business = currentBusiness();
        Optional<Expense> byId = expenseRepo.findById(id);
        if (byId.isPresent() && byId.get().getBusiness().equals(business)) {
            Expense e = byId.get();
            expenseRepo.delete(e);
            return toDto(e);
        }
        return null;
    }

    @Override
    public List<ExpenseDto> getAllExpenses() {
        Business business = currentBusiness();
        List<Expense> all = expenseRepo.findAllByBusiness(business);
        List<ExpenseDto> dtos = new ArrayList<>();
        for (Expense e : all) dtos.add(toDto(e));
        return dtos;
    }

    @Override
    public ExpenseDto getExpenseById(Long id) {
        Business business = currentBusiness();
        return expenseRepo.findById(id).filter(e -> e.getBusiness().equals(business)).map(this::toDto).orElse(null);
    }
}