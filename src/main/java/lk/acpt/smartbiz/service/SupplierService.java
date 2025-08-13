package lk.acpt.smartbiz.service;

import lk.acpt.smartbiz.dto.SupplierDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SupplierService {
    SupplierDto saveSupplier(SupplierDto supplier);
    SupplierDto deleteSupplier(Long id);
    List<SupplierDto> getAllSuppliers();
    SupplierDto updateSupplier(SupplierDto supplier);
    SupplierDto getSupplierById(Long id);
    SupplierDto getSupplierByEmail(String email);
}
