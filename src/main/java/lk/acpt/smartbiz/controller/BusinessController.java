package lk.acpt.smartbiz.controller;

import lk.acpt.smartbiz.dto.BusinessRequest;
import lk.acpt.smartbiz.dto.BusinessResponse;
import lk.acpt.smartbiz.service.BusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/business")
public class BusinessController {

    @Autowired
    private BusinessService businessService;

    @PostMapping("/register")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<BusinessResponse> register(@RequestBody BusinessRequest request, Authentication auth) {
        String email = auth.getName();
        BusinessResponse resp = businessService.createBusiness(request, email);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BusinessResponse> myBusiness(Authentication auth) {
        String email = auth.getName();
        BusinessResponse resp = businessService.getBusinessForOwner(email);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BusinessResponse>> allBusinesses() {
        return ResponseEntity.ok(businessService.listAllBusinesses());
    }
}