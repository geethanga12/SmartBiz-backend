package lk.acpt.smartbiz.controller;

import lk.acpt.smartbiz.dto.CustomerDto;
import lk.acpt.smartbiz.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/customer")
@PreAuthorize("hasRole('OWNER')") // Owner-only
public class CustomerController {

    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<?> saveCustomer(@RequestBody CustomerDto customerDto) {
        CustomerDto result = customerService.saveCustomer(customerDto);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteCustomer(@PathVariable Long id) {
        CustomerDto result = customerService.deleteCustomer(id);
        if (result != null) return new ResponseEntity<>(result, HttpStatus.OK);

        Map<String, Object> res = new HashMap<>();
        res.put("result", "No customer with id " + id + " was found");
        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
    }

    @GetMapping
    public ResponseEntity<List<CustomerDto>> getAllCustomer() {
        return new ResponseEntity<>(customerService.getAllCustomers(), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateCustomer(@RequestBody CustomerDto customerDto, @PathVariable Long id) {
        customerDto.setId(id);
        CustomerDto updated = customerService.updateCustomer(customerDto);
        if (updated != null) return new ResponseEntity<>(updated, HttpStatus.OK);

        Map<String, Object> res = new HashMap<>();
        res.put("result", "No customer with id " + id + " was found");
        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getCustomerById(@PathVariable Long id) {
        CustomerDto customer = customerService.getCustomerById(id);
        if (customer != null) return new ResponseEntity<>(customer, HttpStatus.OK);

        Map<String, Object> res = new HashMap<>();
        res.put("result", "No customer found with id " + id);
        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/get_by_email/{email}")
    public ResponseEntity<Object> getCustomerByEmail(@PathVariable String email) {
        CustomerDto customerByEmail = customerService.getCustomerByEmail(email);
        if (customerByEmail != null) return new ResponseEntity<>(customerByEmail, HttpStatus.OK);

        Map<String, Object> res = new HashMap<>();
        res.put("result", "No customer with email " + email + " was found");
        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
    }
}
