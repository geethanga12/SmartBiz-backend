package lk.acpt.smartbiz.service;

import lk.acpt.smartbiz.dto.EmployeeDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface EmployeeService {
    EmployeeDto saveEmployee(EmployeeDto dto);
    EmployeeDto deleteEmployee(Long id);
    List<EmployeeDto> getAllEmployees();
    EmployeeDto updateEmployee(EmployeeDto dto);
    EmployeeDto getEmployeeById(Long id);
    EmployeeDto getEmployeeByEmail(String email);
}