package lk.acpt.smartbiz.service.impl;

import lk.acpt.smartbiz.dto.EmployeeDto;
import lk.acpt.smartbiz.entity.Business;
import lk.acpt.smartbiz.entity.Employee;
import lk.acpt.smartbiz.entity.User;
import lk.acpt.smartbiz.repo.BusinessRepository;
import lk.acpt.smartbiz.repo.EmployeeRepository;
import lk.acpt.smartbiz.repo.UserRepository;
import lk.acpt.smartbiz.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepo;
    private final UserRepository userRepo;
    private final BusinessRepository businessRepo;
    private final PasswordEncoder passwordEncoder; // Inject for password hashing

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepo, UserRepository userRepo, BusinessRepository businessRepo, PasswordEncoder passwordEncoder) {
        this.employeeRepo = employeeRepo;
        this.userRepo = userRepo;
        this.businessRepo = businessRepo;
        this.passwordEncoder = passwordEncoder;
    }

    private Business currentBusiness() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User owner = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("Owner not found"));
        return businessRepo.findByOwner(owner).orElseThrow(() -> new RuntimeException("Business not found"));
    }

    private EmployeeDto toDto(Employee e) {
        return new EmployeeDto(
                e.getEmployeeId(),
                e.getName(),
                e.getPassword(), // Do not return password in DTO for security; null it if needed
                e.getRole(),
                e.getSalary(),
                e.getEmail()
        );
    }

    @Override
    public EmployeeDto saveEmployee(EmployeeDto dto) {
        Business business = currentBusiness();

        if (employeeRepo.findByEmailAndBusiness(dto.getEmail(), business).isPresent()) {
            throw new RuntimeException("Employee with this email already exists for your business");
        }

        Employee employee = new Employee();
        employee.setBusiness(business);
        employee.setName(dto.getName());
        employee.setPassword(passwordEncoder.encode(dto.getPassword()));
        employee.setRole(dto.getRole());
        employee.setSalary(dto.getSalary());
        employee.setEmail(dto.getEmail());
        Employee saved = employeeRepo.save(employee);
        return toDto(saved);
    }

    @Override
    public EmployeeDto deleteEmployee(Long id) {
        Business business = currentBusiness();
        Optional<Employee> byId = employeeRepo.findByEmployeeIdAndBusiness(id, business);

        if (byId.isPresent()) {
            Employee e = byId.get();
            employeeRepo.delete(e);
            return toDto(e);
        }
        return null;
    }

    @Override
    public List<EmployeeDto> getAllEmployees() {
        Business business = currentBusiness();
        List<Employee> all = employeeRepo.findAllByBusiness(business);
        List<EmployeeDto> dtos = new ArrayList<>();
        for (Employee e : all) dtos.add(toDto(e));
        return dtos;
    }

    @Override
    public EmployeeDto updateEmployee(EmployeeDto dto) {
        Business business = currentBusiness();
        Optional<Employee> byId = employeeRepo.findByEmployeeIdAndBusiness(dto.getId(), business);

        if (byId.isPresent()) {
            Employee e = byId.get();
            e.setName(dto.getName());
            if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                e.setPassword(passwordEncoder.encode(dto.getPassword()));
            }
            e.setRole(dto.getRole());
            e.setSalary(dto.getSalary());
            e.setEmail(dto.getEmail());
            Employee updated = employeeRepo.save(e);
            return toDto(updated);
        }
        return null;
    }

    @Override
    public EmployeeDto getEmployeeById(Long id) {
        Business business = currentBusiness();
        return employeeRepo.findByEmployeeIdAndBusiness(id, business).map(this::toDto).orElse(null);
    }

    @Override
    public EmployeeDto getEmployeeByEmail(String email) {
        Business business = currentBusiness();
        return employeeRepo.findByEmailAndBusiness(email, business).map(this::toDto).orElse(null);
    }
}