package lk.acpt.smartbiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsDto {
    private long totalBusinesses;
    private long totalUsers;
    private long totalAIRequests;
    private double totalAICosts;
    private Map<String, Long> businessesByStatus;
    private Map<String, Long> aiRequestsByType;
    private List<UsageLogDto> recentLogs;
    private Map<String, Object> systemHealth;
}