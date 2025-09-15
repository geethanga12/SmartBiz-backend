package lk.acpt.smartbiz.service;

import lk.acpt.smartbiz.dto.AIRequestDto;
import lk.acpt.smartbiz.dto.AIResponseDto;
import lk.acpt.smartbiz.dto.BusinessInsightDto;
import lk.acpt.smartbiz.dto.EmailTemplateDto;

public interface AIService {
    AIResponseDto processAIRequest(AIRequestDto request, String userEmail);
    BusinessInsightDto generateBusinessInsights(String question, String userEmail);
    EmailTemplateDto generateEmail(String type, String context, String userEmail);
    String generateMarketingPost(String productInfo, String promotion, String userEmail);
    String generateInvoiceSummary(Long orderId, String userEmail);
}