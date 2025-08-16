package lk.acpt.smartbiz.controller;

import lk.acpt.smartbiz.dto.EmployeeDto;
import lk.acpt.smartbiz.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/employee")
@PreAuthorize("hasRole('OWNER')")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<?> saveEmployee(@RequestBody EmployeeDto dto) {
        EmployeeDto result = employeeService.saveEmployee(dto);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteEmployee(@PathVariable Long id) {
        EmployeeDto result = employeeService.deleteEmployee(id);
        if (result != null) return new ResponseEntity<>(result, HttpStatus.OK);

        Map<String, Object> res = new HashMap<>();
        res.put("result", "No employee with id " + id + " was found");
        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
    }

    @GetMapping
    public ResponseEntity<List<EmployeeDto>> getAllEmployees() {
        return new ResponseEntity<>(employeeService.getAllEmployees(), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateEmployee(@RequestBody EmployeeDto dto, @PathVariable Long id) {
        dto.setId(id);
        EmployeeDto updated = employeeService.updateEmployee(dto);
        if (updated != null) return new ResponseEntity<>(updated, HttpStatus.OK);

        Map<String, Object> res = new HashMap<>();
        res.put("result", "No employee with id " + id + " was found");
        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getEmployeeById(@PathVariable Long id) {
        EmployeeDto employee = employeeService.getEmployeeById(id);
        if (employee != null) return new ResponseEntity<>(employee, HttpStatus.OK);

        Map<String, Object> res = new HashMap<>();
        res.put("result", "No employee found with id " + id);
        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/get_by_email/{email}")
    public ResponseEntity<Object> getEmployeeByEmail(@PathVariable String email) {
        EmployeeDto byEmail = employeeService.getEmployeeByEmail(email);
        if (byEmail != null) return new ResponseEntity<>(byEmail, HttpStatus.OK);

        Map<String, Object> res = new HashMap<>();
        res.put("result", "No employee with email " + email + " was found");
        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
    }
}