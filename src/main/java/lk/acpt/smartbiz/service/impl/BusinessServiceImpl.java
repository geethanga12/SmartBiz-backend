package lk.acpt.smartbiz.service.impl;

import lk.acpt.smartbiz.dto.BusinessRequest;
import lk.acpt.smartbiz.dto.BusinessResponse;
import lk.acpt.smartbiz.entity.Business;
import lk.acpt.smartbiz.entity.User;
import lk.acpt.smartbiz.repo.BusinessRepository;
import lk.acpt.smartbiz.repo.UserRepository;
import lk.acpt.smartbiz.service.BusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BusinessServiceImpl implements BusinessService {

    @Autowired
    private BusinessRepository businessRepo;

    @Autowired
    private UserRepository userRepo;

    @Override
    public BusinessResponse createBusiness(BusinessRequest req, String ownerEmail) {
        User owner = userRepo.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        // if owner already has business - you can return or throw
        if (businessRepo.findByOwner(owner).isPresent()) {
            throw new RuntimeException("Owner already has a business");
        }

        Business b = new Business();
        b.setOwner(owner);
        b.setBusinessName(req.getBusinessName());
        b.setAddress(req.getAddress());
        b.setRegisterDate(LocalDateTime.now());
        b.setStatus("ACTIVE");
        Business saved = businessRepo.save(b);

        return toDto(saved);
    }

    @Override
    public BusinessResponse getBusinessForOwner(String ownerEmail) {
        User owner = userRepo.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Owner not found"));
        Business b = businessRepo.findByOwner(owner)
                .orElseThrow(() -> new RuntimeException("Business not found"));
        return toDto(b);
    }

    @Override
    public List<BusinessResponse> listAllBusinesses() {
        return businessRepo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    private BusinessResponse toDto(Business b) {
        return new BusinessResponse(
                b.getBusinessId(),
                b.getOwner().getUserId(),
                b.getOwner().getEmail(),
                b.getBusinessName(),
                b.getAddress(),
                b.getStatus(),
                b.getRegisterDate()
        );
    }
}