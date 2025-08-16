package lk.acpt.smartbiz.repo;

import lk.acpt.smartbiz.entity.Business;
import lk.acpt.smartbiz.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findAllByBusiness(Business business);
    Optional<Employee> findByEmployeeIdAndBusiness(Long employeeId, Business business);
    Optional<Employee> findByEmailAndBusiness(String email, Business business);
}