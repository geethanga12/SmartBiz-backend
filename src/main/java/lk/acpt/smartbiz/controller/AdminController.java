package lk.acpt.smartbiz.controller;

import lk.acpt.smartbiz.dto.AdminStatsDto;
import lk.acpt.smartbiz.dto.BusinessResponse;
import lk.acpt.smartbiz.dto.UsageLogDto;
import lk.acpt.smartbiz.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDto> getSystemStats() {
        AdminStatsDto stats = adminService.getSystemStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/businesses")
    public ResponseEntity<List<BusinessResponse>> getAllBusinesses() {
        List<BusinessResponse> businesses = adminService.getAllBusinesses();
        return ResponseEntity.ok(businesses);
    }

    @GetMapping("/logs/usage")
    public ResponseEntity<List<UsageLogDto>> getUsageLogs(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "50") int size) {
        List<UsageLogDto> logs = adminService.getUsageLogs(page, size);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/logs/ai")
    public ResponseEntity<List<UsageLogDto>> getAIUsageLogs(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "50") int size) {
        List<UsageLogDto> logs = adminService.getAIUsageLogs(page, size);
        return ResponseEntity.ok(logs);
    }
}