package lk.acpt.smartbiz.service;

import lk.acpt.smartbiz.dto.BusinessRequest;
import lk.acpt.smartbiz.dto.BusinessResponse;

import java.util.List;

public interface BusinessService {
    BusinessResponse createBusiness(BusinessRequest req, String ownerEmail);
    BusinessResponse getBusinessForOwner(String ownerEmail);
    List<BusinessResponse> listAllBusinesses();
}