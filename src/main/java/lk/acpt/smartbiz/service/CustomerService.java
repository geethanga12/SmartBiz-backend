package lk.acpt.smartbiz.service;

import lk.acpt.smartbiz.dto.CustomerDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CustomerService {
    CustomerDto saveCustomer(CustomerDto customer);
    CustomerDto deleteCustomer(Long id);
    List<CustomerDto> getAllCustomers();
    CustomerDto updateCustomer(CustomerDto customer);
    CustomerDto getCustomerById(Long id);
    CustomerDto getCustomerByEmail(String email);
}
