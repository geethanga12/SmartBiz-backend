package lk.acpt.smartbiz.controller;

import lk.acpt.smartbiz.dto.ReportRequestDto;
import lk.acpt.smartbiz.service.AdminService;
import lk.acpt.smartbiz.service.ReportsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@PreAuthorize("hasRole('OWNER')")
public class ReportsController {

    @Autowired
    private ReportsService reportsService;

    @Autowired
    private AdminService adminService;

    @PostMapping("/sales")
    public ResponseEntity<Map<String, Object>> generateSalesReport(@RequestBody ReportRequestDto request,
                                                                   Authentication auth,
                                                                   HttpServletRequest httpRequest) {
        try {
            adminService.logUserAction(
                    auth.getName(),
                    "GENERATE_SALES_REPORT",
                    "Period: " + request.getStartDate() + " to " + request.getEndDate(),
                    httpRequest.getRemoteAddr(),
                    httpRequest.getHeader("User-Agent")
            );

            Map<String, Object> report = reportsService.generateSalesReport(request, auth.getName());
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/inventory")
    public ResponseEntity<Map<String, Object>> generateInventoryReport(@RequestBody ReportRequestDto request,
                                                                       Authentication auth,
                                                                       HttpServletRequest httpRequest) {
        try {
            adminService.logUserAction(
                    auth.getName(),
                    "GENERATE_INVENTORY_REPORT",
                    "Include details: " + request.isIncludeDetails(),
                    httpRequest.getRemoteAddr(),
                    httpRequest.getHeader("User-Agent")
            );

            Map<String, Object> report = reportsService.generateInventoryReport(request, auth.getName());
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/profit-loss")
    public ResponseEntity<Map<String, Object>> generateProfitLossReport(@RequestBody ReportRequestDto request,
                                                                        Authentication auth,
                                                                        HttpServletRequest httpRequest) {
        try {
            adminService.logUserAction(
                    auth.getName(),
                    "GENERATE_PROFIT_LOSS_REPORT",
                    "Period: " + request.getStartDate() + " to " + request.getEndDate(),
                    httpRequest.getRemoteAddr(),
                    httpRequest.getHeader("User-Agent")
            );

            Map<String, Object> report = reportsService.generateProfitLossReport(request, auth.getName());
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/customer-analysis")
    public ResponseEntity<Map<String, Object>> generateCustomerAnalysisReport(@RequestBody ReportRequestDto request,
                                                                              Authentication auth,
                                                                              HttpServletRequest httpRequest) {
        try {
            adminService.logUserAction(
                    auth.getName(),
                    "GENERATE_CUSTOMER_ANALYSIS_REPORT",
                    "Period: " + request.getStartDate() + " to " + request.getEndDate(),
                    httpRequest.getRemoteAddr(),
                    httpRequest.getHeader("User-Agent")
            );

            Map<String, Object> report = reportsService.generateCustomerAnalysisReport(request, auth.getName());
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/pdf/{reportType}")
    public ResponseEntity<byte[]> generatePDFReport(@PathVariable String reportType,
                                                    @RequestBody ReportRequestDto request,
                                                    Authentication auth,
                                                    HttpServletRequest httpRequest) {
        try {
            adminService.logUserAction(
                    auth.getName(),
                    "GENERATE_PDF_REPORT",
                    "Report type: " + reportType,
                    httpRequest.getRemoteAddr(),
                    httpRequest.getHeader("User-Agent")
            );

            Map<String, Object> reportData;
            switch (reportType.toLowerCase()) {
                case "sales":
                    reportData = reportsService.generateSalesReport(request, auth.getName());
                    break;
                case "inventory":
                    reportData = reportsService.generateInventoryReport(request, auth.getName());
                    break;
                case "profit-loss":
                    reportData = reportsService.generateProfitLossReport(request, auth.getName());
                    break;
                case "customer-analysis":
                    reportData = reportsService.generateCustomerAnalysisReport(request, auth.getName());
                    break;
                default:
                    throw new RuntimeException("Unknown report type: " + reportType);
            }

            byte[] pdfBytes = reportsService.generatePDFReport(reportType, reportData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", reportType + "_report.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}