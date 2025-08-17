package lk.acpt.smartbiz.repo;

import lk.acpt.smartbiz.entity.Business;
import lk.acpt.smartbiz.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findAllByBusiness(Business business);

    // For dashboard
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.business = ?1 AND e.date >= ?2 AND e.date < ?3")
    Double getExpensesBetweenDates(Business business, LocalDateTime start, LocalDateTime end);
}