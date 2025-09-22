package lk.acpt.smartbiz.service;

import lk.acpt.smartbiz.dto.AdminStatsDto;
import lk.acpt.smartbiz.dto.BusinessResponse;
import lk.acpt.smartbiz.dto.UsageLogDto;

import java.util.List;
import java.util.Map;

public interface AdminService {
    // Existing methods
    AdminStatsDto getSystemStats();
    List<BusinessResponse> getAllBusinesses();
    List<UsageLogDto> getUsageLogs(int page, int size);
    List<UsageLogDto> getAIUsageLogs(int page, int size);
    void logUserAction(String userEmail, String action, String details, String ipAddress, String userAgent);

    // NEW: Enhanced analytics and monitoring methods
    Map<String, Object> getSystemWideStatistics(int days);
    Map<String, Object> getAIUsageStatistics(int days);
    List<Map<String, Object>> getBusinessActivity(int days);
    Map<String, Object> getUserEngagementMetrics(int days);
    Map<String, Object> getSubscriptionAnalytics();
    Map<String, Object> getRevenueMetrics(int days);
    Map<String, Object> getSystemHealth();
    Map<String, Object> exportUsageLogs(int days, String format);
    Map<String, Object> getFeatureUsage(int days);
}