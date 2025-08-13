package lk.acpt.smartbiz.service.impl;

import lk.acpt.smartbiz.dto.CustomerDto;
import lk.acpt.smartbiz.entity.Business;
import lk.acpt.smartbiz.entity.Customer;
import lk.acpt.smartbiz.entity.User;
import lk.acpt.smartbiz.repo.BusinessRepository;
import lk.acpt.smartbiz.repo.CustomerRepository;
import lk.acpt.smartbiz.repo.UserRepository;
import lk.acpt.smartbiz.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceIMPL implements CustomerService {

    private final CustomerRepository customerRepo;
    private final UserRepository userRepo;
    private final BusinessRepository businessRepo;

    @Autowired
    public CustomerServiceIMPL(CustomerRepository customerRepo,
                               UserRepository userRepo,
                               BusinessRepository businessRepo) {
        this.customerRepo = customerRepo;
        this.userRepo = userRepo;
        this.businessRepo = businessRepo;
    }

    private Business currentBusiness() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User owner = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("Owner not found"));
        return businessRepo.findByOwner(owner).orElseThrow(() -> new RuntimeException("Business not found"));
    }

    private CustomerDto toDto(Customer c) {
        return new CustomerDto(
                c.getCustomerId(),
                c.getCustomerName(),
                c.getEmail(),
                c.getPhone(),
                c.getAddress()
        );
    }

    @Override
    public CustomerDto saveCustomer(CustomerDto customerDto) {
        Business business = currentBusiness();

        // Optional: prevent duplicate email within same business
        if (customerRepo.findByEmailAndBusiness(customerDto.getEmail(), business).isPresent()) {
            throw new RuntimeException("Customer with this email already exists for your business");
        }

        Customer saved = customerRepo.save(new Customer(
                null,
                business,
                customerDto.getName(),
                customerDto.getEmail(),
                customerDto.getPhone(),
                customerDto.getAddress()
        ));
        return toDto(saved);
    }

    @Override
    public CustomerDto deleteCustomer(Long id) {
        Business business = currentBusiness();
        //Optional<Customer> byId = customerRepo.findByIdAndBusiness(id, business);
        Optional<Customer> byId = customerRepo.findByCustomerIdAndBusiness(id, business);

        if (byId.isPresent()) {
            Customer c = byId.get();
            customerRepo.delete(c);
            return toDto(c);
        }
        return null;
    }

    @Override
    public List<CustomerDto> getAllCustomers() {
        Business business = currentBusiness();
        List<Customer> all = customerRepo.findAllByBusiness(business);
        List<CustomerDto> dtos = new ArrayList<>();
        for (Customer c : all) dtos.add(toDto(c));
        return dtos;
    }

    @Override
    public CustomerDto updateCustomer(CustomerDto dto) {
        Business business = currentBusiness();
        //Optional<Customer> byId = customerRepo.findByIdAndBusiness(dto.getId(), business);
        Optional<Customer> byId = customerRepo.findByCustomerIdAndBusiness(dto.getId(), business);

        if (byId.isPresent()) {
            Customer c = byId.get();
            c.setCustomerName(dto.getName());
            c.setEmail(dto.getEmail());
            c.setPhone(dto.getPhone());
            c.setAddress(dto.getAddress());
            Customer update = customerRepo.save(c);
            return toDto(update);
        }
        return null;
    }

    @Override
    public CustomerDto getCustomerById(Long id) {
        Business business = currentBusiness();
        //return customerRepo.findByIdAndBusiness(id, business).map(this::toDto).orElse(null);
        return customerRepo.findByCustomerIdAndBusiness(id, business)
                .map(this::toDto)
                .orElse(null);
    }

    @Override
    public CustomerDto getCustomerByEmail(String email) {
        Business business = currentBusiness();
        return customerRepo.findByEmailAndBusiness(email, business).map(this::toDto).orElse(null);
    }
}
