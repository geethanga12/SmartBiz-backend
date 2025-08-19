package lk.acpt.smartbiz.service.impl;

import lk.acpt.smartbiz.entity.Business;
import lk.acpt.smartbiz.entity.User;
import lk.acpt.smartbiz.repo.*;
import lk.acpt.smartbiz.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepo;
    private final ExpenseRepository expenseRepo;
    private final ItemRepository itemRepo;
    private final OrderDetailRepository detailRepo; // For costs
    private final UserRepository userRepo;
    private final BusinessRepository businessRepo;

    @Autowired
    public DashboardServiceImpl(OrderRepository orderRepo, ExpenseRepository expenseRepo, ItemRepository itemRepo, OrderDetailRepository detailRepo, UserRepository userRepo, BusinessRepository businessRepo) {
        this.orderRepo = orderRepo;
        this.expenseRepo = expenseRepo;
        this.itemRepo = itemRepo;
        this.detailRepo = detailRepo;
        this.userRepo = userRepo;
        this.businessRepo = businessRepo;
    }

    private Business currentBusiness() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User owner = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("Owner not found"));
        return businessRepo.findByOwner(owner).orElseThrow(() -> new RuntimeException("Business not found"));
    }

    @Override
    public Map<String, Object> getOverview() {
        Business business = currentBusiness();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDay = now.truncatedTo(ChronoUnit.DAYS);
        LocalDateTime endDay = startDay.plusDays(1);
        double sales = orderRepo.getSalesBetweenDates(business, startDay, endDay) != null ? orderRepo.getSalesBetweenDates(business, startDay, endDay) : 0.0;
        double expenses = expenseRepo.getExpensesBetweenDates(business, startDay, endDay) != null ? expenseRepo.getExpensesBetweenDates(business, startDay, endDay) : 0.0;
        double inventoryValue = itemRepo.getInventoryValue(business) != null ? itemRepo.getInventoryValue(business) : 0.0;

        // Profits: sales - expenses - costs (costs from today's orders)
        double costs = 0.0;
        costs = detailRepo.getCostsBetweenDates(business, startDay, endDay) != null ? detailRepo.getCostsBetweenDates(business, startDay, endDay) : 0.0;

        double profits = sales - expenses - costs;
        Map<String, Object> overview = new HashMap<>();
        overview.put("sales", sales);
        overview.put("inventoryValue", inventoryValue);
        overview.put("profits", profits);
        overview.put("expenses", expenses);
        // Add more like top items, etc.
        return overview;
    }
}