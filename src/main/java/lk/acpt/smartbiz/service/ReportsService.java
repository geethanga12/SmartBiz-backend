package lk.acpt.smartbiz.service;

import lk.acpt.smartbiz.dto.ReportRequestDto;

import java.util.Map;

public interface ReportsService {
    Map<String, Object> generateSalesReport(ReportRequestDto request, String userEmail);
    Map<String, Object> generateInventoryReport(ReportRequestDto request, String userEmail);
    Map<String, Object> generateProfitLossReport(ReportRequestDto request, String userEmail);
    Map<String, Object> generateCustomerAnalysisReport(ReportRequestDto request, String userEmail);
    byte[] generatePDFReport(String reportType, Map<String, Object> reportData);
}