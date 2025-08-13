package lk.acpt.smartbiz.service.impl;

import lk.acpt.smartbiz.dto.SupplierDto;
import lk.acpt.smartbiz.entity.Business;
import lk.acpt.smartbiz.entity.Supplier;
import lk.acpt.smartbiz.entity.User;
import lk.acpt.smartbiz.repo.BusinessRepository;
import lk.acpt.smartbiz.repo.SupplierRepository;
import lk.acpt.smartbiz.repo.UserRepository;
import lk.acpt.smartbiz.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SupplierServiceIMPL implements SupplierService {

    private final SupplierRepository supplierRepo;
    private final UserRepository userRepo;
    private final BusinessRepository businessRepo;

    @Autowired
    public SupplierServiceIMPL(SupplierRepository supplierRepo,
                               UserRepository userRepo,
                               BusinessRepository businessRepo) {
        this.supplierRepo = supplierRepo;
        this.userRepo = userRepo;
        this.businessRepo = businessRepo;
    }

    private Business currentBusiness() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User owner = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("Owner not found"));
        return businessRepo.findByOwner(owner).orElseThrow(() -> new RuntimeException("Business not found"));
    }

    private SupplierDto toDto(Supplier s) {
        return new SupplierDto(
                s.getSupplierId(),
                s.getName(),
                s.getEmail(),
                s.getPhone(),
                s.getAddress()
        );
    }

    @Override
    public SupplierDto saveSupplier(SupplierDto dto) {
        Business business = currentBusiness();

        if (supplierRepo.findByEmailAndBusiness(dto.getEmail(), business).isPresent()) {
            throw new RuntimeException("Supplier with this email already exists for your business");
        }

        Supplier saved = supplierRepo.save(new Supplier(
                null,
                business,
                dto.getName(),
                dto.getEmail(),
                dto.getPhone(),
                dto.getAddress()
        ));
        return toDto(saved);
    }

    @Override
    public SupplierDto deleteSupplier(Long id) {
        Business business = currentBusiness();
        //Optional<Supplier> byId = supplierRepo.findByIdAndBusiness(id, business);
        Optional<Supplier> byId = supplierRepo.findBySupplierIdAndBusiness(id, business);

        if (byId.isPresent()) {
            Supplier s = byId.get();
            supplierRepo.delete(s);
            return toDto(s);
        }
        return null;
    }

    @Override
    public List<SupplierDto> getAllSuppliers() {
        Business business = currentBusiness();
        List<Supplier> all = supplierRepo.findAllByBusiness(business);
        List<SupplierDto> dtos = new ArrayList<>();
        for (Supplier s : all) dtos.add(toDto(s));
        return dtos;
    }

    @Override
    public SupplierDto updateSupplier(SupplierDto dto) {
        Business business = currentBusiness();
        //Optional<Supplier> byId = supplierRepo.findByIdAndBusiness(dto.getId(), business);
        Optional<Supplier> byId = supplierRepo.findBySupplierIdAndBusiness(dto.getId(), business);

        if (byId.isPresent()) {
            Supplier s = byId.get();
            s.setName(dto.getName());
            s.setEmail(dto.getEmail());
            s.setPhone(dto.getPhone());
            s.setAddress(dto.getAddress());
            Supplier update = supplierRepo.save(s);
            return toDto(update);
        }
        return null;
    }

    @Override
    public SupplierDto getSupplierById(Long id) {
        Business business = currentBusiness();
        //return supplierRepo.findByIdAndBusiness(id, business).map(this::toDto).orElse(null);
        return supplierRepo.findBySupplierIdAndBusiness(id, business)
                .map(this::toDto)
                .orElse(null);
    }

    @Override
    public SupplierDto getSupplierByEmail(String email) {
        Business business = currentBusiness();
        return supplierRepo.findByEmailAndBusiness(email, business).map(this::toDto).orElse(null);
    }
}
