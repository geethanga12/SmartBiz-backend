package lk.acpt.smartbiz.repo;

import lk.acpt.smartbiz.entity.Business;
import lk.acpt.smartbiz.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findAllByBusiness(Business business);
    Optional<Customer> findByIdAndBusiness(Long id, Business business);
    Optional<Customer> findByEmailAndBusiness(String email, Business business);
}
