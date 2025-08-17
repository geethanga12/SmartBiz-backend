package lk.acpt.smartbiz.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface DashboardService {
    Map<String, Object> getOverview(); // Sales, inventory, profits, etc.
}