package lk.acpt.smartbiz.repo;

import lk.acpt.smartbiz.entity.Business;
import lk.acpt.smartbiz.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findAllByBusiness(Business business);
    //Optional<Supplier> findByIdAndBusiness(Long id, Business business);
    Optional<Supplier> findBySupplierIdAndBusiness(Long supplierId, Business business);
    Optional<Supplier> findByEmailAndBusiness(String email, Business business);
}
