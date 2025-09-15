package lk.acpt.smartbiz.service;

import lk.acpt.smartbiz.dto.AdminStatsDto;
import lk.acpt.smartbiz.dto.BusinessResponse;
import lk.acpt.smartbiz.dto.UsageLogDto;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminService {
    AdminStatsDto getSystemStats();
    List<BusinessResponse> getAllBusinesses();
    List<UsageLogDto> getUsageLogs(int page, int size);
    List<UsageLogDto> getAIUsageLogs(int page, int size);
    void logUserAction(String userEmail, String action, String details, String ipAddress, String userAgent);
}